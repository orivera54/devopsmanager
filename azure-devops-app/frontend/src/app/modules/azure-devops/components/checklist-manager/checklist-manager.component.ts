import { Component, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ChecklistService } from '../../services/checklist.service'; // Assuming path is correct
import { Checklist, ChecklistItem, CreateChecklistPayload, CreateChecklistItemPayload, UpdateChecklistItemPayload } from '../../models/azdevops.models'; // Assuming path

@Component({
  selector: 'app-checklist-manager',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './checklist-manager.component.html',
  styleUrls: ['./checklist-manager.component.css']
})
export class ChecklistManagerComponent implements OnInit, OnChanges {
  @Input() azureDevopsOrganization!: string;
  @Input() azureDevopsProjectName!: string;
  @Input() azureDevopsWorkItemId!: number;

  checklists: Checklist[] = [];
  isLoading = false;
  error: string | null = null;

  newChecklistForm: FormGroup;
  newChecklistItemForms: { [checklistId: number]: FormGroup } = {};

  constructor(
    private checklistService: ChecklistService,
    private fb: FormBuilder
  ) {
    this.newChecklistForm = this.fb.group({
      name: ['', Validators.required],
      description: ['']
    });
  }

  ngOnInit(): void {
    this.loadChecklistsIfInputsReady();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['azureDevopsOrganization'] || changes['azureDevopsProjectName'] || changes['azureDevopsWorkItemId']) {
      this.loadChecklistsIfInputsReady();
    }
  }

  private inputsReady(): boolean {
    const ready = !!this.azureDevopsOrganization && !!this.azureDevopsProjectName && !!this.azureDevopsWorkItemId;
    if (!ready) {
        // console.warn("ChecklistManager: Inputs not ready.",
        //              "Org:", this.azureDevopsOrganization,
        //              "Project:", this.azureDevopsProjectName,
        //              "WI ID:", this.azureDevopsWorkItemId);
    }
    return ready;
  }

  private loadChecklistsIfInputsReady(): void {
    if (this.inputsReady()) {
      this.loadChecklists();
    }
  }

  loadChecklists(): void {
    if (!this.inputsReady()) {
      this.error = "Work item identifiers (organization, project, work item ID) are missing or invalid.";
      console.error(this.error);
      this.isLoading = false; // Ensure loading is stopped
      return;
    }
    this.isLoading = true;
    this.error = null;
    this.checklistService.getChecklistsForWorkItem(
      this.azureDevopsOrganization,
      this.azureDevopsProjectName,
      this.azureDevopsWorkItemId
    ).subscribe({
      next: (data) => {
        this.checklists = data.sort((a, b) => new Date(a.createdAt!).getTime() - new Date(b.createdAt!).getTime()); // Sort by creation time
        this.checklists.forEach(cl => {
            this.sortChecklistItems(cl); // Sort items within each checklist
            this.initNewChecklistItemForm(cl.id);
        });
        this.isLoading = false;
      },
      error: (err) => {
        this.error = `Failed to load checklists: ${err.message || 'Unknown error'}`;
        if(err.error?.message) this.error += ` Server: ${err.error.message}`;
        console.error(this.error, err);
        this.isLoading = false;
      }
    });
  }

  initNewChecklistItemForm(checklistId: number): void {
    this.newChecklistItemForms[checklistId] = this.fb.group({
      itemText: ['', Validators.required],
      itemOrder: [0]
    });
  }

  createChecklist(): void {
    if (this.newChecklistForm.invalid) {
      this.error = "Checklist name is required.";
      return;
    }
    if (!this.inputsReady()) {
        this.error = "Cannot create checklist: Work item identifiers are missing.";
        return;
    }
    const payload: CreateChecklistPayload = {
      name: this.newChecklistForm.value.name,
      description: this.newChecklistForm.value.description || '', // Ensure description is not null
      azureDevopsOrganization: this.azureDevopsOrganization,
      azureDevopsProjectName: this.azureDevopsProjectName,
      azureDevopsWorkItemId: this.azureDevopsWorkItemId,
      items: []
    };
    this.isLoading = true;
    this.checklistService.createChecklist(payload).subscribe({
      next: (newChecklist) => {
        this.loadChecklists(); // Reload to get all checklists including the new one with its ID
        this.newChecklistForm.reset();
        // initNewChecklistItemForm will be called for the new checklist within loadChecklists
      },
      error: (err) => {
        this.error = `Failed to create checklist: ${err.message || 'Unknown error'}`;
        if(err.error?.message) this.error += ` Server: ${err.error.message}`;
        this.isLoading = false;
      }
    });
  }

  deleteChecklist(checklistId: number, event: MouseEvent): void {
    event.stopPropagation();
    if (confirm('Are you sure you want to delete this checklist and all its items?')) {
        this.isLoading = true;
        this.checklistService.deleteChecklist(checklistId).subscribe({
            next: () => {
                this.loadChecklists();
            },
            error: (err) => {
                this.error = `Failed to delete checklist: ${err.message || 'Unknown error'}`;
                if(err.error?.message) this.error += ` Server: ${err.error.message}`;
                this.isLoading = false;
            }
        });
    }
  }

  addChecklistItem(checklistId: number): void {
    const formGroup = this.newChecklistItemForms[checklistId];
    if (!formGroup || formGroup.invalid) {
        this.error = "Item text is required to add an item.";
        return;
    }
    const currentChecklist = this.checklists.find(c => c.id === checklistId);
    const nextOrder = currentChecklist && currentChecklist.items ? currentChecklist.items.length : 0;

    const itemPayload: CreateChecklistItemPayload = {
      itemText: formGroup.value.itemText,
      itemOrder: formGroup.value.itemOrder === 0 ? nextOrder : (formGroup.value.itemOrder || nextOrder),
      isCompleted: false
    };
    this.isLoading = true;
    this.checklistService.addChecklistItem(checklistId, itemPayload).subscribe({
      next: (newItem) => {
        // For more robust update, reload all checklists or at least the affected one
        this.loadChecklists();
        // formGroup.reset({ itemOrder: 0 }); // Resetting is handled by loadChecklists re-init
      },
      error: (err) => {
        this.error = `Failed to add item to checklist ${checklistId}: ${err.message || 'Unknown error'}`;
        if(err.error?.message) this.error += ` Server: ${err.error.message}`;
        this.isLoading = false;
      }
    });
  }

  toggleItemCompletion(item: ChecklistItem, checklistId: number): void {
    const payload: UpdateChecklistItemPayload = { isCompleted: !item.isCompleted };
    // this.isLoading = true; // Can set a specific item-level loading if needed
    this.checklistService.updateChecklistItem(item.id, payload).subscribe({
      next: (updatedItem) => {
        const checklist = this.checklists.find(c => c.id === checklistId);
        if (checklist) {
            const itemIndex = checklist.items.findIndex(i => i.id === item.id);
            if (itemIndex > -1) {
                checklist.items[itemIndex] = { ...checklist.items[itemIndex], ...updatedItem }; // Update local item
            }
        }
        // this.isLoading = false;
      },
      error: (err) => {
        this.error = `Failed to update item ${item.id}: ${err.message || 'Unknown error'}`;
        if(err.error?.message) this.error += ` Server: ${err.error.message}`;
        item.isCompleted = !item.isCompleted; // Revert optimistic update on error
        // this.isLoading = false;
      }
    });
  }

  deleteChecklistItem(checklistId: number, itemId: number, event: MouseEvent): void {
    event.stopPropagation();
    if (confirm('Are you sure you want to delete this item?')) {
        // this.isLoading = true; // Item-specific or global loading
        this.checklistService.deleteChecklistItem(itemId).subscribe({
            next: () => {
                const checklist = this.checklists.find(c => c.id === checklistId);
                if (checklist) {
                    checklist.items = checklist.items.filter(it => it.id !== itemId);
                    // No need to sort again unless order is affected by deletion (it's not)
                }
                // this.isLoading = false;
            },
            error: (err) => {
                this.error = `Failed to delete item ${itemId}: ${err.message || 'Unknown error'}`;
                if(err.error?.message) this.error += ` Server: ${err.error.message}`;
                // this.isLoading = false;
            }
        });
    }
  }

  private sortChecklistItems(checklist: Checklist): void {
    if (checklist && checklist.items) {
        checklist.items.sort((a, b) => a.itemOrder - b.itemOrder);
    }
  }
}
