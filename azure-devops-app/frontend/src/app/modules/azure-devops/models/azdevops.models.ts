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
