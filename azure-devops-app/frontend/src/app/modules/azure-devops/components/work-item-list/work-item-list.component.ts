import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router'; // RouterModule for routerLink
import { AzureDevopsService } from '../../services/azure-devops.service';
import { AzDevOpsWorkItem } from '../../models/azdevops.models';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; // For future filters

@Component({
  selector: 'app-work-item-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './work-item-list.component.html',
  styleUrls: ['./work-item-list.component.css']
})
export class WorkItemListComponent implements OnInit {
  workItems: AzDevOpsWorkItem[] = [];
  isLoading = true;
  error: string | null = null;
  projectIdOrName: string | null = null;

  filterState: string = '';
  filterType: string = '';
  filterAssignedTo: string = '';

  constructor(
    private azureDevopsService: AzureDevopsService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.projectIdOrName = params.get('projectIdOrName');
      if (this.projectIdOrName) {
        this.loadWorkItems();
      } else {
        this.isLoading = false;
        const errorMessage = 'Project ID or Name not found in route parameters.';
        this.error = errorMessage;
        console.error(errorMessage);
      }
    });
  }

  private buildWiqlQuery(): string {
    if (!this.projectIdOrName) {
      console.error("Cannot build WIQL query: Project ID or Name is missing.");
      return '';
    }

    let conditions: string[] = [];
    // IMPORTANT: In WIQL, @project refers to the project context of the query (passed in URL or team context).
    // So, it's typically not needed to explicitly state [System.TeamProject] = @project in the WIQL string itself
    // if the API endpoint already scopes to a project. However, if querying at organization level, it's needed.
    // The current backend `queryWorkItems` endpoint is /api/azdevops/projects/{projectOrTeamName}/workitems/query
    // So, the project context IS set by the URL. We can simplify the WIQL.
    // conditions.push(`[System.TeamProject] = @project`); // This is usually implicit with project-scoped API calls.
                                                        // If it were an org-level WIQL API, you'd use:
                                                        // conditions.push(`[System.TeamProject] = '${this.projectIdOrName.replace(/'/g, "''")}'`);


    if (this.filterState && this.filterState.trim() !== '') {
      conditions.push(`[System.State] = '${this.filterState.trim().replace(/'/g, "''")}'`);
    }
    if (this.filterType && this.filterType.trim() !== '') {
      conditions.push(`[System.WorkItemType] = '${this.filterType.trim().replace(/'/g, "''")}'`);
    }
    if (this.filterAssignedTo && this.filterAssignedTo.trim() !== '') {
      conditions.push(`[System.AssignedTo] = '${this.filterAssignedTo.trim().replace(/'/g, "''")}'`);
    }

    const whereClause = conditions.length > 0 ? `WHERE ${conditions.join(' AND ')}` : '';
    // Requesting specific fields. If $expand=All is used in service, these might be redundant but good for clarity.
    // The backend service currently passes 'All' for $expand to queryWorkItems.
    // The WIQL SELECT clause determines the columns in the WIQL response, $expand gets more complete objects for those IDs.
    return `SELECT [System.Id], [System.Title], [System.State], [System.WorkItemType], [System.AssignedTo], [System.ChangedDate] FROM workitems ${whereClause} ORDER BY [System.ChangedDate] DESC`;
  }

  loadWorkItems(): void {
    if (!this.projectIdOrName) {
      this.error = "Project ID or Name is missing. Cannot load work items.";
      this.isLoading = false;
      console.error(this.error);
      return;
    }

    const wiql = this.buildWiqlQuery();
    if (!wiql) { // Should not happen if projectIdOrName is present
        this.error = "Could not build WIQL query.";
        this.isLoading = false;
        console.error(this.error);
        return;
    }

    this.isLoading = true;
    this.error = null;

    console.log(`Executing WIQL for project '${this.projectIdOrName}': ${wiql}`);

    // The service's queryWorkItems takes project name/ID for the URL, and WIQL for the body.
    this.azureDevopsService.queryWorkItems(this.projectIdOrName, wiql, 'All').subscribe({
      next: (data) => {
        this.workItems = data;
        this.isLoading = false;
        if(data.length === 0) {
            console.info(`No work items found for project '${this.projectIdOrName}' with current filters.`);
        }
      },
      error: (err) => {
        console.error(`Error fetching work items for project '${this.projectIdOrName}' with WIQL: ${wiql}`, err);
        let displayError = `Failed to load work items.`;
        if (err.message) { displayError += ` Message: ${err.message}`; }

        if (err.error) {
            if (typeof err.error === 'string' && err.error.length < 250) { // Avoid overly long server HTML errors
                 displayError += ` Server Details: ${err.error}`;
            } else if (err.error.message) { // Standard Spring Boot error response
                 displayError += ` Server Details: ${err.error.message}`;
            } else if (err.error.error && err.error.error.message) { // Deeper nested error
                 displayError += ` Server Details: ${err.error.error.message}`;
            }
        }
        this.error = displayError;
        this.isLoading = false;
      }
    });
  }

  applyFilters(): void {
    this.loadWorkItems();
  }

  clearFilters(): void {
    this.filterState = '';
    this.filterType = '';
    this.filterAssignedTo = '';
    this.loadWorkItems();
  }

  navigateToDetail(workItemId: number): void {
    // Navigate to a future detail page, e.g., /azure-devops/workitems/detail/{workItemId}
    // This route would need to be added to azure-devops-routing.module.ts
    this.router.navigate(['/azure-devops', 'workitems', 'detail', workItemId]);
  }
}
