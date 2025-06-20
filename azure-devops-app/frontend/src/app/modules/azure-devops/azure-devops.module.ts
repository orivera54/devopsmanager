import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http'; // HttpClientModule might be needed if service isn't root provided or for clarity
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AzureDevopsRoutingModule } from './azure-devops-routing.module';

// Services (if scoped to this module, otherwise they are providedIn: 'root')
// import { AzureDevopsService } from './services/azure-devops.service';

// Components that belong to this module
import { PlaceholderAzdevopsComponent } from './components/placeholder-azdevops/placeholder-azdevops.component';
// import { AzDashboardComponent } from './components/az-dashboard/az-dashboard.component';
// import { ProjectListComponent } from './components/project-list/project-list.component';
// import { WorkItemDetailComponent } from './components/work-item-detail/work-item-detail.component';


@NgModule({
  declarations: [
    PlaceholderAzdevopsComponent, // Declare the placeholder component
    // AzDashboardComponent,
    // ProjectListComponent,
    // WorkItemDetailComponent,
  ],
  imports: [
    CommonModule,
    AzureDevopsRoutingModule,
    // HttpClientModule, // Only if AzureDevopsService is provided here and not in 'root'.
                       // If service is 'root', AppModule's HttpClientModule is sufficient.
    FormsModule,
    ReactiveFormsModule
  ],
  // providers: [AzureDevopsService] // Only if service is scoped to this module
})
export class AzureDevopsModule { }
