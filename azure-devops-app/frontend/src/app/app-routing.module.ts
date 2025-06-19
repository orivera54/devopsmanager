import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { authGuard } from './guards/auth.guard';

// Placeholder for DashboardComponent, actual import would be needed when component exists
// import { DashboardComponent } from './components/dashboard/dashboard.component';
// Placeholder for PageNotFoundComponent
// import { PageNotFoundComponent } from './components/page-not-found/page-not-found.component';

const routes: Routes = [
  // Auth routes are eagerly loaded or standalone as they are part of the core app flow
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },

  // Example of an eagerly loaded Dashboard route (if it's a central part of the app shell)
  // {
  //   path: 'dashboard',
  //   // component: DashboardComponent, // This component needs to be created
  //   canActivate: [authGuard]
  // },

  // --- Lazy Loaded Feature Modules ---
  {
    path: 'azure-devops', // Base path for the Azure DevOps feature module
    loadChildren: () => import('./modules/azure-devops/azure-devops.module').then(m => m.AzureDevopsModule),
    canActivate: [authGuard] // Secure the entire feature module
  },
  // Example for a future Checklist module:
  // {
  //   path: 'checklist', // Base path for the Checklist feature module
  //   loadChildren: () => import('./modules/checklist/checklist.module').then(m => m.ChecklistModule),
  //   canActivate: [authGuard] // Secure the entire feature module
  // },

  // Default route: Redirect to login, or to a dashboard if already logged in (logic for this redirect might be in a guard or AppComponent)
  { path: '', redirectTo: '/login', pathMatch: 'full' },

  // Wildcard route for a 404 page (PageNotFoundComponent)
  // { path: '**', component: PageNotFoundComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
