import { Component, OnInit } from '@angular/core';
import { AzureDevopsService } from '../../services/azure-devops.service';
import { AzDevOpsProject } from '../../models/azdevops.models';
import { CommonModule } from '@angular/common'; // For *ngFor, *ngIf, date pipe, etc.
import { RouterModule } from '@angular/router'; // For routerLink (if needed later)

@Component({
  selector: 'app-project-list',
  standalone: true, // Modern Angular component style
  imports: [CommonModule, RouterModule], // Import modules needed by the template
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.css']
})
export class ProjectListComponent implements OnInit {
  projects: AzDevOpsProject[] = [];
  isLoading = true;
  error: string | null = null;

  constructor(private azureDevopsService: AzureDevopsService) {}

  ngOnInit(): void {
    this.isLoading = true;
    this.azureDevopsService.getProjects().subscribe({
      next: (data) => {
        this.projects = data;
        this.isLoading = false;
        if (data.length === 0) {
          console.info('No Azure DevOps projects found for this organization or PAT.');
        }
      },
      error: (err) => {
        console.error('Error fetching Azure DevOps projects:', err);
        this.error = 'Failed to load projects. Check console for details and ensure backend is running & configured.';
        this.isLoading = false;
      }
    });
  }
}
