// This file is now consolidated into dashboardService.ts
// Keeping only additional types that might be needed

export interface DashboardStats {
  label: string;
  value: number;
  change?: number;
  changeType?: "increase" | "decrease";
  icon?: string;
}

export interface DashboardWidget {
  id: string;
  title: string;
  type: "stat" | "chart" | "list";
  data: any;
  loading?: boolean;
  error?: string;
}
