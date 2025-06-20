import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AzureDevopsService } from '../../services/azure-devops.service';
import { AzDevOpsWorkItem } from '../../models/azdevops.models';
import { ChartConfiguration, ChartData, ChartEvent, ChartType } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { WorkItemTimeAlert } from '../../models/azdevops.models'; // Import WorkItemTimeAlert
import { Router, RouterModule } from '@angular/router'; // Import Router and RouterModule

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, BaseChartDirective, RouterModule], // Added RouterModule
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  projectIdOrName: string | null = null;
  workItems: AzDevOpsWorkItem[] = []; // For general dashboard metrics based on work items
  isLoading = true; // General loading for primary dashboard data
  error: string | null = null;

  // Time Alerts specific properties
  timeAlerts: WorkItemTimeAlert[] = [];
  isLoadingAlerts = false;

  // Metrics
  totalEstimatedHours: number = 0;
  totalCompletedWork: number = 0;
  totalRemainingWork: number = 0;
  percentageCompleted: number = 0;
  openWorkItems: number = 0;
  closedWorkItems: number = 0;

  private routeSub: Subscription | undefined;
  private workItemsSub: Subscription | undefined;

  // Pie Chart for Work Item Status
  public workItemStatusPieChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: true, position: 'top' },
      title: { display: true, text: 'Work Item Status Distribution' }
    }
  };
  public workItemStatusPieChartData: ChartData<'pie', number[], string | string[]> = {
    labels: [],
    datasets: [{ data: [], backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40'] }]
  };
  public workItemStatusPieChartType: ChartType = 'pie';

  // Bar Chart for Work Item Hours
  public workItemHoursBarChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    scales: { x: {}, y: { beginAtZero: true, title: { display: true, text: 'Hours' } } },
    plugins: {
      legend: { display: true, position: 'top' },
      title: { display: true, text: 'Work Item Hours Overview' }
    }
  };
  public workItemHoursBarChartData: ChartData<'bar'> = {
    labels: ['Estimated', 'Completed', 'Remaining'],
    datasets: [
      { data: [], label: 'Hours', backgroundColor: ['rgba(255, 99, 132, 0.7)', 'rgba(54, 162, 235, 0.7)', 'rgba(255, 206, 86, 0.7)'] }
    ]
  };
  public workItemHoursBarChartType: ChartType = 'bar';

  constructor(
    private route: ActivatedRoute,
    private azureDevopsService: AzureDevopsService,
    private router: Router // Injected Router
  ) {}

  ngOnInit(): void {
    this.routeSub = this.route.paramMap.subscribe(params => {
      this.projectIdOrName = params.get('projectIdOrName');
      if (this.projectIdOrName) {
        this.loadDashboardData(); // Loads main metrics and charts
        this.loadTimeAlerts();    // Loads time alerts separately
      } else {
        this.isLoading = false; // Ensure general isLoading is false
        this.isLoadingAlerts = false; // And alerts loading
        this.error = 'Project ID or Name not provided in route. Cannot load dashboard.';
        console.warn(this.error);
      }
    });
  }

  ngOnDestroy(): void {
    this.routeSub?.unsubscribe();
    this.workItemsSub?.unsubscribe();
    // Remember to unsubscribe from timeAlertsSub if you add it
  }

  loadDashboardData(): void {
    if (!this.projectIdOrName) return;
    this.isLoading = true;
    this.error = null;

    const wiql = `SELECT [System.Id], [System.State], [Microsoft.VSTS.Scheduling.OriginalEstimate], [Microsoft.VSTS.Scheduling.CompletedWork], [Microsoft.VSTS.Scheduling.RemainingWork] FROM workitems WHERE [System.TeamProject] = @project`;

    this.workItemsSub = this.azureDevopsService.queryWorkItems(this.projectIdOrName, wiql, 'Fields').subscribe({
      next: (data) => {
        this.workItems = data;
        this.calculateMetrics();
        this.prepareChartData(); // This will trigger chart updates
        this.isLoading = false;
      },
      error: (err) => {
        this.error = `Failed to load work items for dashboard: ${err.message || 'Unknown error'}`;
        if (err.error?.message) this.error += ` Server: ${err.error.message}`;
        this.isLoading = false;
        console.error(this.error, err);
      }
    });
  }

  calculateMetrics(): void {
    // Reset metrics
    this.totalEstimatedHours = 0;
    this.totalCompletedWork = 0;
    this.totalRemainingWork = 0;
    this.openWorkItems = 0;
    this.closedWorkItems = 0;
    const statusCounts: { [key: string]: number } = {};

    this.workItems.forEach(item => {
      const fields = item.fields;
      if (fields) {
        this.totalEstimatedHours += fields['Microsoft.VSTS.Scheduling.OriginalEstimate'] || 0;
        this.totalCompletedWork += fields['Microsoft.VSTS.Scheduling.CompletedWork'] || 0;
        this.totalRemainingWork += fields['Microsoft.VSTS.Scheduling.RemainingWork'] || 0;

        const state = fields['System.State'] || 'Unknown';
        statusCounts[state] = (statusCounts[state] || 0) + 1;

        if (['Closed', 'Done', 'Removed'].includes(state)) {
          this.closedWorkItems++;
        } else {
          this.openWorkItems++;
        }
      }
    });

    if (this.totalEstimatedHours > 0) {
      this.percentageCompleted = Math.min(100, (this.totalCompletedWork / this.totalEstimatedHours) * 100);
    } else {
      this.percentageCompleted = (this.workItems.length > 0 && this.openWorkItems === 0 && this.closedWorkItems > 0) ? 100 : 0;
    }

    this.workItemStatusPieChartData.labels = Object.keys(statusCounts);
    this.workItemStatusPieChartData.datasets[0].data = Object.values(statusCounts);

    this.workItemHoursBarChartData.datasets[0].data = [
      this.totalEstimatedHours,
      this.totalCompletedWork,
      this.totalRemainingWork
    ];
  }

  prepareChartData(): void {
    // Cloning the data objects to ensure change detection triggers chart refresh
    this.workItemStatusPieChartData = { ...this.workItemStatusPieChartData, datasets: [{ ...this.workItemStatusPieChartData.datasets[0] }]  };
    this.workItemHoursBarChartData = { ...this.workItemHoursBarChartData, datasets: [{ ...this.workItemHoursBarChartData.datasets[0] }] };
  }

  public chartClicked({ event, active }: { event?: ChartEvent, active?: {}[] }): void {
    // console.log('Chart Clicked', event, active);
  }

  public chartHovered({ event, active }: { event?: ChartEvent, active?: {}[] }): void {
    // console.log('Chart Hovered', event, active);
  }

  loadTimeAlerts(): void {
    if (!this.projectIdOrName) return;
    this.isLoadingAlerts = true;
    // Default states for alerts, could be configurable
    const defaultStatesForAlerts = ['Active', 'In Progress', 'Committed'];
    // daysThreshold is optional in service, not passing it here to use service default or backend default
    this.azureDevopsService.getTimeReportAlerts(this.projectIdOrName, defaultStatesForAlerts).subscribe({
      next: (alerts) => {
        this.timeAlerts = alerts;
        this.isLoadingAlerts = false;
      },
      error: (err) => {
        console.error('Failed to load time alerts for project ' + this.projectIdOrName, err);
        // Display a specific error for this section or log it, but don't block the main dashboard
        // this.error = 'Failed to load time alerts.'; // Or a more specific error display area
        this.isLoadingAlerts = false;
      }
    });
  }

  navigateToWorkItem(workItemId: number): void {
    // Navigate to the work item detail page
    this.router.navigate(['/azure-devops', 'workitems', 'detail', workItemId]);
  }
}
