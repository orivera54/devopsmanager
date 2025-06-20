import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AzureDevopsService } from '../../services/azure-devops.service';
import { AzDevOpsWorkItem, AzDevOpsComment, AzDevOpsUserReference, UpdateWorkItemPayload } from '../../models/azdevops.models';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { SafeHtmlPipe } from '../../../../pipes/safe-html.pipe';
import { ChecklistManagerComponent } from '../checklist-manager/checklist-manager.component'; // Import ChecklistManager

@Component({
  selector: 'app-work-item-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    SafeHtmlPipe,
    ChecklistManagerComponent // Add ChecklistManagerComponent here
  ],
  templateUrl: './work-item-detail.component.html',
  styleUrls: ['./work-item-detail.component.css']
})
export class WorkItemDetailComponent implements OnInit, OnDestroy {
  workItem: AzDevOpsWorkItem | null = null;
  comments: AzDevOpsComment[] = [];
  isLoading = true;
  isEditing = false;
  error: string | null = null;
  workItemId: number | null = null;

  workItemForm: FormGroup;
  private routeSubscription: Subscription | undefined;
  private workItemSubscription: Subscription | undefined;
  private commentsSubscription: Subscription | undefined;
  private updateSubscription: Subscription | undefined;


  availableStates: string[] = ['New', 'Active', 'Resolved', 'Closed', 'Removed', 'Proposed', 'Committed', 'Done']; // Example, adjust as per your AzDO process
  availableReasons: { [key: string]: string[] } = { // Example reasons
    'New': ['New'],
    'Active': ['Implementation started', 'Approved'],
    'Committed': ['Committed'],
    'Done': ['Work finished', 'Requirements met'],
    'Resolved': ['Code complete and all tests pass', 'Fixed and verified'],
    'Closed': ['Accepted', 'Closed', 'Resolved as duplicate', 'Resolved as by design'],
    'Removed': ['Removed from backlog']
  };
  // availableTypes: string[] = ['User Story', 'Task', 'Bug', 'Epic', 'Feature'];

  // Placeholder for organization name. This needs a proper way to be set,
  // e.g., from a global config service, route data, or fetched with project details.
  // For now, using a placeholder that developers must configure or enhance.
  public currentOrganization: string = "YOUR_AZDO_ORG_PLACEHOLDER"; // TODO: Replace with actual org name source

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private azureDevopsService: AzureDevopsService,
    private fb: FormBuilder
  ) {
    this.workItemForm = this.fb.group({
      title: ['', Validators.required],
      state: [''],
      reason: [''],
      assignedTo: [''],
      description: [''],
      priority: [null, [Validators.min(1), Validators.max(4)]], // AzDO priority is 1-4 usually
      originalEstimate: [null, [Validators.min(0)]],
      remainingWork: [null, [Validators.min(0)]],
      completedWork: [null, [Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    this.routeSubscription = this.route.paramMap.subscribe(params => {
      const id = params.get('workItemId');
      if (id) {
        this.workItemId = +id;
        this.loadWorkItemDetails();
        this.loadComments();
      } else {
        this.isLoading = false;
        this.error = 'Work Item ID not found in route.';
        console.error(this.error);
      }
    });
  }

  ngOnDestroy(): void {
    this.routeSubscription?.unsubscribe();
    this.workItemSubscription?.unsubscribe();
    this.commentsSubscription?.unsubscribe();
    this.updateSubscription?.unsubscribe();
  }

  loadWorkItemDetails(): void {
    if (!this.workItemId) return;
    this.isLoading = true;
    this.error = null;
    this.workItemSubscription = this.azureDevopsService.getWorkItemById(this.workItemId, undefined, 'All').subscribe({
      next: (data) => {
        this.workItem = data;
        if (data) {
          this.populateForm();
        } else {
          this.error = `Work item with ID ${this.workItemId} not found.`;
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.error = `Failed to load work item ${this.workItemId}: ${err.message || 'Unknown server error'}`;
        if (err.error?.message) this.error += ` Server: ${err.error.message}`;
        this.isLoading = false;
        console.error(this.error, err);
      }
    });
  }

  loadComments(): void {
    if (!this.workItemId) return;
    this.commentsSubscription = this.azureDevopsService.getWorkItemComments(this.workItemId, 'renderedText').subscribe({
      next: (data) => {
        this.comments = data;
      },
      error: (err) => {
        console.error('Failed to load comments for work item ' + this.workItemId, err);
        // Non-critical, so don't set global error or stop loading indicator for this alone
      }
    });
  }

  populateForm(): void {
    if (this.workItem && this.workItem.fields) {
      this.workItemForm.patchValue({
        title: this.workItem.fields["System.Title"],
        state: this.workItem.fields["System.State"],
        reason: this.workItem.fields["System.Reason"],
        assignedTo: this.workItem.fields["System.AssignedTo"]?.uniqueName || this.workItem.fields["System.AssignedTo"]?.displayName || '',
        description: this.workItem.fields["System.DescriptionHtml"] || this.workItem.fields["System.Description"],
        priority: this.workItem.fields["Microsoft.VSTS.Common.Priority"],
        originalEstimate: this.workItem.fields["Microsoft.VSTS.Scheduling.OriginalEstimate"],
        remainingWork: this.workItem.fields["Microsoft.VSTS.Scheduling.RemainingWork"],
        completedWork: this.workItem.fields["Microsoft.VSTS.Scheduling.CompletedWork"]
      });
    }
  }

  toggleEdit(): void {
    this.isEditing = !this.isEditing;
    if (this.isEditing && this.workItem) {
      this.populateForm();
    }
  }

  saveChanges(): void {
    if (!this.workItemForm.valid || !this.workItemId) {
      this.error = 'Form is invalid or Work Item ID is missing.';
      console.error(this.error, this.workItemForm.errors);
      return;
    }
    this.isLoading = true;
    const formValues = this.workItemForm.value;

    const payload: UpdateWorkItemPayload = { // Using the defined interface
        title: formValues.title,
        state: formValues.state,
        description: formValues.description,
        priority: formValues.priority,
        originalEstimate: formValues.originalEstimate,
        remainingWork: formValues.remainingWork,
        completedWork: formValues.completedWork,
        assignedTo: formValues.assignedTo === '' ? null : formValues.assignedTo, // Convert empty string to null for unassignment
    };

    // Only include reason if state changed or reason itself changed and is valid for the state
    if (this.workItem?.fields["System.State"] !== formValues.state && formValues.reason) {
        payload.reason = formValues.reason;
    } else if (this.workItem?.fields["System.State"] === formValues.state && this.workItem?.fields["System.Reason"] !== formValues.reason) {
        payload.reason = formValues.reason;
    }


    this.updateSubscription = this.azureDevopsService.updateWorkItem(this.workItemId, payload).subscribe({
      next: (updatedWorkItem) => {
        this.workItem = updatedWorkItem;
        this.isLoading = false;
        this.isEditing = false;
        // TODO: Add success notification (e.g., toaster)
      },
      error: (err) => {
        this.error = `Failed to update work item ${this.workItemId}: ${err.message || 'Unknown server error'}`;
        if (err.error?.message) this.error += ` Server: ${err.error.message}`;
        this.isLoading = false;
        console.error(this.error, err);
      }
    });
  }

  getReasonsForState(state: string | null | undefined): string[] {
    if (!state) return [];
    return this.availableReasons[state] || [this.workItemForm.get('reason')?.value || '']; // Keep current reason if state not in map
  }

  goBack(): void {
    // Navigate back to the work item list, trying to preserve project context if possible
    const teamProject = this.workItem?.fields && this.workItem.fields["System.TeamProject"];
    // If the detail page was reached from a project-specific work item list,
    // this.route.parent might give access to 'projectIdOrName' from that route.
    // Example: /azure-devops/projects/MyProject/workitems -> detail page.
    // A more robust solution might involve a navigation service or passing more context via route data.
    const projectIdFromRoute = this.route.snapshot.paramMap.get('projectIdOrName'); // Check if it was part of a parent route

    if (teamProject) { // teamProject is usually the project name or GUID
      this.router.navigate(['/azure-devops/projects', teamProject, 'workitems']);
    } else if (projectIdFromRoute) {
      this.router.navigate(['/azure-devops/projects', projectIdFromRoute, 'workitems']);
    }
    else {
      // Fallback if project context cannot be determined
      this.router.navigate(['/azure-devops/projects']);
    }
  }
}
