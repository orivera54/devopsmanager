import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
    Checklist,
    ChecklistItem,
    CreateChecklistPayload,
    CreateChecklistItemPayload,
    UpdateChecklistItemPayload,
    UpdateChecklistInfoPayload
} from '../models/azdevops.models'; // Adjusted path if models are in a subfolder

@Injectable({
  providedIn: 'root' // Or 'providedIn: AzureDevopsModule' if scoped to the module
})
export class ChecklistService {
  private apiUrl = '/api/checklists'; // Base URL for checklist related APIs

  constructor(private http: HttpClient) {}

  createChecklist(payload: CreateChecklistPayload): Observable<Checklist> {
    return this.http.post<Checklist>(this.apiUrl, payload);
  }

  getChecklistsForWorkItem(organization: string, project: string, workItemId: number): Observable<Checklist[]> {
    const params = new HttpParams()
      .set('organization', organization)
      .set('project', project)
      .set('workItemId', workItemId.toString());
    return this.http.get<Checklist[]>(`${this.apiUrl}/for-workitem`, { params });
  }

  getChecklistById(checklistId: number): Observable<Checklist> {
    return this.http.get<Checklist>(`${this.apiUrl}/${checklistId}`);
  }

  updateChecklistInfo(checklistId: number, payload: UpdateChecklistInfoPayload): Observable<Checklist> {
    return this.http.put<Checklist>(`${this.apiUrl}/${checklistId}/info`, payload);
  }

  deleteChecklist(checklistId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${checklistId}`);
  }

  addChecklistItem(checklistId: number, itemPayload: CreateChecklistItemPayload): Observable<ChecklistItem> {
    return this.http.post<ChecklistItem>(`${this.apiUrl}/${checklistId}/items`, itemPayload);
  }

  updateChecklistItem(itemId: number, itemPayload: UpdateChecklistItemPayload): Observable<ChecklistItem> {
    // Using PATCH for partial updates of an item
    return this.http.patch<ChecklistItem>(`${this.apiUrl}/items/${itemId}`, itemPayload);
  }

  deleteChecklistItem(itemId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/items/${itemId}`);
  }
}
