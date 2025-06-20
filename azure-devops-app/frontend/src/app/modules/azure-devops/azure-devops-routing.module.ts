import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

// Import components when they are created, for example:
// import { AzDashboardComponent } from './components/az-dashboard/az-dashboard.component';
import { ProjectListComponent } from './components/project-list/project-list.component';
import { WorkItemListComponent } from './components/work-item-list/work-item-list.component';
import { PlaceholderAzdevopsComponent } from './components/placeholder-azdevops/placeholder-azdevops.component';
import { WorkItemDetailComponent } from './components/work-item-detail/work-item-detail.component';
import { DashboardComponent } from './components/dashboard/dashboard.component'; // Import DashboardComponent

const routes: Routes = [
  {
    path: '',
    component: PlaceholderAzdevopsComponent,
    children: [
      { path: 'projects', component: ProjectListComponent },
      { path: 'projects/:projectIdOrName/workitems', component: WorkItemListComponent },
      { path: 'projects/:projectIdOrName/dashboard', component: DashboardComponent }, // New route for dashboard
      { path: 'workitems/detail/:workItemId', component: WorkItemDetailComponent },
      { path: '', redirectTo: 'projects', pathMatch: 'full' }
    ]
  }
  // Other top-level routes for this module could go here, for example:
  // { path: 'settings', component: AzDevOpsSettingsComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AzureDevopsRoutingModule { }
