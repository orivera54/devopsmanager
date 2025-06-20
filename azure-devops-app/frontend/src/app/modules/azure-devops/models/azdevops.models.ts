export interface AzDevOpsProject {
  id: string;
  name: string;
  description?: string;
  url?: string;
  state?: string;
  lastUpdateTime?: string; // Or Date if transformed during service call
}

export interface AzDevOpsWorkItem {
  id: number;
  rev: number;
  fields: AzDevOpsWorkItemFields;
  url?: string;
  _links?: { // From backend DTO
    html?: { href: string };
    parent?: { href: string };
    // other links...
  };
  htmlLink?: string; // Optional helper property you might populate after fetching
}

export interface AzDevOpsWorkItemFields {
  [key: string]: any; // Allow other fields not explicitly defined, helpful for custom fields

  "System.TeamProject"?: string;
  "System.WorkItemType"?: string;
  "System.Title"?: string;
  "System.State"?: string;
  "System.Reason"?: string;
  "System.AssignedTo"?: AzDevOpsUserReference;
  "System.CreatedDate"?: string; // Or Date
  "System.CreatedBy"?: AzDevOpsUserReference;
  "System.ChangedDate"?: string; // Or Date
  "System.ChangedBy"?: AzDevOpsUserReference;
  "System.Description"?: string;     // Plain text
  "System.DescriptionHtml"?: string; // HTML content

  "Microsoft.VSTS.Common.Priority"?: number;
  "Microsoft.VSTS.Scheduling.OriginalEstimate"?: number;
  "Microsoft.VSTS.Scheduling.CompletedWork"?: number;
  "Microsoft.VSTS.Scheduling.RemainingWork"?: number;

  "System.IterationPath"?: string;
  "System.AreaPath"?: string;
}

export interface AzDevOpsUserReference {
  displayName?: string;
  id?: string;
  uniqueName?: string;
  imageUrl?: string;
}

export interface AzDevOpsComment {
  commentId: number; // Renamed from commentIdActual for consistency, maps to 'id' in JSON
  revision?: number;
  text?: string; // Markdown text
  renderedText?: string; // HTML text if $expand=renderedText
  createdBy?: AzDevOpsUserReference;
  createdDate?: string; // Or Date
  modifiedBy?: AzDevOpsUserReference;
  modifiedDate?: string; // Or Date
  url?: string; // API URL of the comment
}

// Payload for updating a work item
export interface UpdateWorkItemPayload {
  title?: string;
  state?: string;
  reason?: string | null; // Allow null to clear reason
  assignedTo?: string | null; // Allow null to unassign
  description?: string;
  originalEstimate?: number | null;
  completedWork?: number | null;
  remainingWork?: number | null;
  priority?: number | null;
  customFields?: { [key: string]: any }; // For adding/updating custom fields
}

export interface WorkItemTimeAlert {
  workItemId: number;
  title?: string;
  workItemType?: string;
  assignedToDisplayName?: string;
  assignedToUniqueName?: string;
  state?: string;
  changedDate?: string; // Or Date, if transformed
  completedWork?: number | null;
  alertMessage?: string;
}

// --- Checklist Models ---

export interface ChecklistItem {
  id: number;
  itemText: string;
  isCompleted: boolean;
  itemOrder: number;
}

export interface Checklist {
  id: number;
  name: string;
  description?: string;
  azureDevopsOrganization: string;
  azureDevopsProjectName: string;
  azureDevopsWorkItemId: number;
  createdByUserId?: number;
  createdByUsername?: string;
  createdAt?: string; // Or Date
  updatedAt?: string; // Or Date
  items: ChecklistItem[];
}

export interface CreateChecklistItemPayload {
  itemText: string;
  isCompleted?: boolean; // Defaults to false on backend if not provided
  itemOrder?: number;    // Defaults to 0 or next available on backend if not provided
}

export interface CreateChecklistPayload {
  name: string;
  description?: string;
  azureDevopsOrganization: string;
  azureDevopsProjectName: string;
  azureDevopsWorkItemId: number;
  items?: CreateChecklistItemPayload[]; // Optional: create items along with checklist
}

export interface UpdateChecklistInfoPayload { // For updating only checklist's own info
    name: string;
    description?: string;
}

export interface UpdateChecklistItemPayload {
  itemText?: string;     // Optional: only include fields to be updated
  isCompleted?: boolean; // Using boolean | undefined might be better if false is a valid value to set
  itemOrder?: number;
}
