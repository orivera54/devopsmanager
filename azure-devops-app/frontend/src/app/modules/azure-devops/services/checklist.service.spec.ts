import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ChecklistService } from './checklist.service';
import { Checklist, CreateChecklistPayload, ChecklistItem, CreateChecklistItemPayload, UpdateChecklistItemPayload, UpdateChecklistInfoPayload } from '../models/azdevops.models'; // Ensure this path is correct

describe('ChecklistService', () => {
  let service: ChecklistService;
  let httpMock: HttpTestingController;
  const apiUrl = '/api/checklists'; // As defined in ChecklistService

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ChecklistService]
    });
    service = TestBed.inject(ChecklistService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // Verifies that no requests are outstanding.
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should create a checklist via POST', () => {
    const payload: CreateChecklistPayload = {
      name: 'New Test Checklist',
      azureDevopsOrganization: 'TestOrg',
      azureDevopsProjectName: 'TestProj',
      azureDevopsWorkItemId: 123
    };
    const mockChecklistResponse: Checklist = {
      id: 1,
      name: 'New Test Checklist',
      azureDevopsOrganization: 'TestOrg',
      azureDevopsProjectName: 'TestProj',
      azureDevopsWorkItemId: 123,
      items: []
    };

    service.createChecklist(payload).subscribe(checklist => {
      expect(checklist).toEqual(mockChecklistResponse);
      expect(checklist.id).toBe(1);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(mockChecklistResponse);
  });

  it('should get checklists for a work item via GET', () => {
    const mockChecklists: Checklist[] = [
      { id: 1, name: 'CL1', azureDevopsOrganization: 'OrgA', azureDevopsProjectName: 'ProjA', azureDevopsWorkItemId: 101, items: [] },
      { id: 2, name: 'CL2', azureDevopsOrganization: 'OrgA', azureDevopsProjectName: 'ProjA', azureDevopsWorkItemId: 101, items: [] }
    ];
    const org = 'OrgA';
    const project = 'ProjA';
    const workItemId = 101;

    service.getChecklistsForWorkItem(org, project, workItemId).subscribe(checklists => {
      expect(checklists.length).toBe(2);
      expect(checklists).toEqual(mockChecklists);
    });

    const expectedUrl = `${apiUrl}/for-workitem?organization=${org}&project=${project}&workItemId=${workItemId}`;
    const req = httpMock.expectOne(expectedUrl);
    expect(req.request.method).toBe('GET');
    req.flush(mockChecklists);
  });

  it('should get a checklist by ID via GET', () => {
    const mockChecklist: Checklist = { id: 1, name: 'Specific CL', azureDevopsOrganization: 'Org', azureDevopsProjectName: 'Proj', azureDevopsWorkItemId: 1, items: [] };
    const checklistId = 1;

    service.getChecklistById(checklistId).subscribe(checklist => {
      expect(checklist).toEqual(mockChecklist);
    });

    const req = httpMock.expectOne(`${apiUrl}/${checklistId}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockChecklist);
  });

  it('should update checklist info via PUT', () => {
    const checklistId = 1;
    const payload: UpdateChecklistInfoPayload = { name: "Updated Name", description: "Updated Desc" };
    const mockResponse: Checklist = { id: checklistId, name: "Updated Name", description: "Updated Desc", azureDevopsOrganization: 'Org', azureDevopsProjectName: 'Proj', azureDevopsWorkItemId: 1, items: [] };

    service.updateChecklistInfo(checklistId, payload).subscribe(checklist => {
      expect(checklist.name).toBe("Updated Name");
    });
    const req = httpMock.expectOne(`${apiUrl}/${checklistId}/info`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(payload);
    req.flush(mockResponse);
  });

  it('should delete a checklist via DELETE', () => {
    const checklistId = 1;
    service.deleteChecklist(checklistId).subscribe(response => {
      expect(response).toBeNull(); // Expecting void response, so flush null
    });
    const req = httpMock.expectOne(`${apiUrl}/${checklistId}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('should add a checklist item via POST', () => {
    const checklistId = 1;
    const itemPayload: CreateChecklistItemPayload = { itemText: "New Item" };
    const mockNewItem: ChecklistItem = { id: 101, itemText: "New Item", isCompleted: false, itemOrder: 0 };

    service.addChecklistItem(checklistId, itemPayload).subscribe(item => {
      expect(item).toEqual(mockNewItem);
    });
    const req = httpMock.expectOne(`${apiUrl}/${checklistId}/items`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(itemPayload);
    req.flush(mockNewItem);
  });

  it('should update a checklist item via PATCH', () => {
    const itemId = 101;
    const itemPayload: UpdateChecklistItemPayload = { isCompleted: true };
    const mockUpdatedItem: ChecklistItem = { id: itemId, itemText: "Existing", isCompleted: true, itemOrder: 0 };

    service.updateChecklistItem(itemId, itemPayload).subscribe(item => {
      expect(item.isCompleted).toBeTrue();
    });
    const req = httpMock.expectOne(`${apiUrl}/items/${itemId}`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(itemPayload);
    req.flush(mockUpdatedItem);
  });

  it('should delete a checklist item via DELETE', () => {
    const itemId = 101;
    service.deleteChecklistItem(itemId).subscribe(response => {
      expect(response).toBeNull();
    });
    const req = httpMock.expectOne(`${apiUrl}/items/${itemId}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null, { status: 204, statusText: 'No Content' });
  });

});
