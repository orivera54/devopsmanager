import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { AzDevOpsProject, AzDevOpsWorkItem, AzDevOpsComment, UpdateWorkItemPayload, WorkItemTimeAlert } from '../models/azdevops.models'; // Added WorkItemTimeAlert

@Injectable({
  providedIn: 'root'
})
export class AzureDevopsService {
  private apiUrl = '/api/azdevops';

  constructor(private http: HttpClient) {}

  getProjects(): Observable<AzDevOpsProject[]> {
    return this.http.get<AzDevOpsProject[]>(`${this.apiUrl}/projects`);
  }

  getWorkItemById(id: number, project?: string, expand: string = 'All'): Observable<AzDevOpsWorkItem> {
    let params = new HttpParams().set('expand', expand);
    if (project && project.trim() !== '') {
      params = params.set('project', project);
    }
    return this.http.get<AzDevOpsWorkItem>(`${this.apiUrl}/workitems/${id}`, { params });
  }

  getWorkItemsByIds(ids: number[], project?: string, expand: string = 'All'): Observable<AzDevOpsWorkItem[]> {
    if (!ids || ids.length === 0) {
      return of([]);
    }
    let params = new HttpParams().set('ids', ids.join(',')).set('expand', expand);
    if (project && project.trim() !== '') {
      params = params.set('project', project);
    }
    return this.http.get<AzDevOpsWorkItem[]>(`${this.apiUrl}/workitems/batch`, { params });
  }

  queryWorkItems(project: string, wiql: string, expand: string = 'All'): Observable<AzDevOpsWorkItem[]> {
    if (!project || project.trim() === '') {
      console.error('Project is required for querying work items.');
      return of([]);
    }
    const payload = { wiql };
    let params = new HttpParams().set('expand', expand);
    // Note: The backend DTO for query request is WorkItemQueryRequestDto which expects {"wiql": "..."}
    // The current 'payload' here is correct.
    return this.http.post<AzDevOpsWorkItem[]>(`${this.apiUrl}/projects/${project}/workitems/query`, payload, { params });
  }

  updateWorkItem(id: number, updateRequest: UpdateWorkItemPayload): Observable<AzDevOpsWorkItem> {
    return this.http.patch<AzDevOpsWorkItem>(`${this.apiUrl}/workitems/${id}`, updateRequest);
  }

  getWorkItemComments(workItemId: number, expand?: string): Observable<AzDevOpsComment[]> {
    let params = new HttpParams();
    if (expand && expand.trim() !== '' && expand.toLowerCase() !== 'none') {
      params = params.set('expand', expand);
    }
    return this.http.get<AzDevOpsComment[]>(`${this.apiUrl}/workitems/${workItemId}/comments`, { params });
  }

  getTimeReportAlerts(projectIdOrName: string, states?: string[], daysThreshold?: number): Observable<WorkItemTimeAlert[]> {
    let params = new HttpParams();
    if (states && states.length > 0) {
      params = params.set('states', states.join(','));
    }
    if (daysThreshold !== undefined && daysThreshold !== null) { // Ensure daysThreshold can be 0
      params = params.set('daysThreshold', daysThreshold.toString());
    }
    return this.http.get<WorkItemTimeAlert[]>(`${this.apiUrl}/projects/${projectIdOrName}/workitems/time-alerts`, { params });
  }
}
