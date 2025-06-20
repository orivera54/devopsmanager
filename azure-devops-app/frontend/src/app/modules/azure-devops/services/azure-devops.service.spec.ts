import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AzureDevopsService } from './azure-devops.service';
import { AzDevOpsProject, AzDevOpsWorkItem, UpdateWorkItemPayload, WorkItemTimeAlert, AzDevOpsComment } from '../models/azdevops.models';

describe('AzureDevopsService', () => {
  let service: AzureDevopsService;
  let httpMock: HttpTestingController;
  const apiUrl = '/api/azdevops'; // As defined in the service

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AzureDevopsService]
    });
    service = TestBed.inject(AzureDevopsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // Ensures that no requests are outstanding.
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getProjects', () => {
    it('should fetch projects and return an array of AzDevOpsProject', () => {
      const mockProjects: AzDevOpsProject[] = [
        { id: 'proj1', name: 'Project Alpha', description: 'First project' },
        { id: 'proj2', name: 'Project Beta', state: 'WellFormed' }
      ];
      service.getProjects().subscribe(projects => {
        expect(projects.length).toBe(2);
        expect(projects).toEqual(mockProjects);
      });
      const req = httpMock.expectOne(`${apiUrl}/projects`);
      expect(req.request.method).toBe('GET');
      req.flush(mockProjects);
    });
  });

  describe('getWorkItemById', () => {
    it('should fetch a single work item by ID', () => {
      const mockWorkItem: AzDevOpsWorkItem = {
        id: 123, rev: 2,
        fields: { "System.Title": 'Sample Task', "System.WorkItemType": 'Task' }
      };
      const workItemId = 123;
      const projectName = 'OmegaProject';

      service.getWorkItemById(workItemId, projectName, 'All').subscribe(item => {
        expect(item).toEqual(mockWorkItem);
      });

      const req = httpMock.expectOne(`${apiUrl}/workitems/${workItemId}?expand=All&project=${projectName}`);
      expect(req.request.method).toBe('GET');
      req.flush(mockWorkItem);
    });
  });

  describe('getWorkItemsByIds', () => {
    it('should fetch a batch of work items by IDs', () => {
        const mockWorkItems: AzDevOpsWorkItem[] = [
            { id: 1, rev: 1, fields: { "System.Title": 'Batch Item 1' } },
            { id: 2, rev: 1, fields: { "System.Title": 'Batch Item 2' } }
        ];
        const ids = [1, 2];
        const projectName = "TestProject";

        service.getWorkItemsByIds(ids, projectName, "Fields").subscribe(items => {
            expect(items.length).toBe(2);
            expect(items).toEqual(mockWorkItems);
        });

        const req = httpMock.expectOne(`${apiUrl}/workitems/batch?ids=1,2&expand=Fields&project=${projectName}`);
        expect(req.request.method).toBe('GET');
        req.flush(mockWorkItems);
    });

    it('should return an empty observable if no IDs are provided', () => {
        service.getWorkItemsByIds([], 'TestProject', 'Fields').subscribe(items => {
            expect(items.length).toBe(0);
        });
        // No HTTP request should be made
        httpMock.expectNone(`${apiUrl}/workitems/batch`);
    });
  });

  describe('queryWorkItems', () => {
    it('should POST a WIQL query and return work items', () => {
        const mockResponse: AzDevOpsWorkItem[] = [{id: 1, rev: 1, fields: {"System.Title": "Found by WIQL"}}];
        const projectName = 'QueryProject';
        const wiql = "SELECT [System.Id] FROM workitems WHERE [System.State] = 'New'";
        const payload = { wiql };

        service.queryWorkItems(projectName, wiql, 'All').subscribe(items => {
            expect(items.length).toBe(1);
            expect(items[0].fields["System.Title"]).toBe("Found by WIQL");
        });

        const req = httpMock.expectOne(`${apiUrl}/projects/${projectName}/workitems/query?expand=All`);
        expect(req.request.method).toBe('POST');
        expect(req.request.body).toEqual(payload);
        req.flush(mockResponse);
    });
  });

  describe('updateWorkItem', () => {
    it('should PATCH to update a work item', () => {
        const workItemId = 456;
        const payload: UpdateWorkItemPayload = { title: 'Updated Title', state: 'Active' };
        const mockUpdatedWorkItem: AzDevOpsWorkItem = {
            id: workItemId, rev: 3,
            fields: { "System.Title": 'Updated Title', "System.State": 'Active' }
        };

        service.updateWorkItem(workItemId, payload).subscribe(item => {
            expect(item).toEqual(mockUpdatedWorkItem);
        });

        const req = httpMock.expectOne(`${apiUrl}/workitems/${workItemId}`);
        expect(req.request.method).toBe('PATCH');
        expect(req.request.body).toEqual(payload);
        req.flush(mockUpdatedWorkItem);
    });
  });

  describe('getWorkItemComments', () => {
    it('should fetch comments for a work item', () => {
        const mockComments: AzDevOpsComment[] = [
            { commentId: 1, text: 'First comment', createdDate: new Date().toISOString() },
            { commentId: 2, text: 'Second comment', createdDate: new Date().toISOString() }
        ];
        const workItemId = 789;

        service.getWorkItemComments(workItemId, 'renderedText').subscribe(comments => {
            expect(comments.length).toBe(2);
            expect(comments).toEqual(mockComments);
        });
        const req = httpMock.expectOne(`${apiUrl}/workitems/${workItemId}/comments?expand=renderedText`);
        expect(req.request.method).toBe('GET');
        req.flush(mockComments);
    });
  });

  describe('getTimeReportAlerts', () => {
    it('should fetch time report alerts', () => {
        const mockAlerts: WorkItemTimeAlert[] = [
            { workItemId: 1, title: 'Task 1', alertMessage: 'No time logged' }
        ];
        const projectName = "AlertProject";
        const states = ["Active", "Committed"];
        const days = 3;

        service.getTimeReportAlerts(projectName, states, days).subscribe(alerts => {
            expect(alerts.length).toBe(1);
            expect(alerts[0].title).toBe('Task 1');
        });
        const req = httpMock.expectOne(`${apiUrl}/projects/${projectName}/workitems/time-alerts?states=Active,Committed&daysThreshold=3`);
        expect(req.request.method).toBe('GET');
        req.flush(mockAlerts);
    });
  });

});
