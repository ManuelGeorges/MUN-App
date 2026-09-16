// src/types.ts

export type Bindings = {
  DB: D1Database;
};

export enum UserRole {
  ADMIN = 'admin',
  CHIEF_ORGANIZER = 'chief_organizer',
  ORGANIZER = 'organizer',
  TEAM_LEADER = 'team_leader',
  TEAM_MEMBER = 'team_member',
  USER = 'user',
}

export enum TeamRole {
  LEADER = 'leader',
  MEMBER = 'member',
}

export enum MealType {
  BREAKFAST = 'breakfast',
  LUNCH = 'lunch',
}

export enum TransactionType {
  RECHARGE = 'recharge',
  DEDUCTION = 'deduction',
}

export enum BroadcastAudience {
  ALL = 'all',
  TEAM = 'team',
  PARTICIPANT = 'participant',
}

export enum AccessAction {
  ENTRY = 'entry',
  EXIT = 'exit',
}

export enum HallState {
  AVAILABLE = 'available',
  BUSY = 'busy',
  FULL = 'full',
}

// Database row definitions
export interface DbUser {
  id: string;
  name: string;
  email: string | null;
  phone: string | null;
  token_balance: number;
  is_active: number;
  role: string;
  team_id: string | null;
  team_role: string | null;
  password_hash: string | null;
  salt: string | null;
  meal_allowance?: number;
  meals_balance?: number;
}

export interface DbTeam {
  id: string;
  name: string;
  capacity: number;
}

export interface DbCard {
  uid: string;
  user_id: string;
  is_active: number;
}

export interface DbMealRecord {
  id: string;
  user_id: string;
  meal_type: string;
  swipe_timestamp: string;
}

export interface DbTransaction {
  id: string;
  user_id: string;
  amount: number;
  transaction_type: string;
  description: string | null;
  created_at: string;
}

export interface DbHall {
  id: string;
  name: string;
  capacity_threshold: number;
  allowed_roles: string;
  current_occupancy: number;
}

export interface DbAccessLog {
  id: string;
  user_id: string;
  hall_id: string;
  action: string;
  timestamp: string;
  allowed: number;
  reason: string | null;
}

export interface DbNotification {
  id: string;
  sender_id: string;
  audience: string;
  team_id: string | null;
  recipient_id: string | null;
  message: string;
  recipient_count: number;
  timestamp: string;
}

export interface DbMealWindow {
  meal_type: string;
  start_time: string;
  end_time: string;
}

// Client response definitions
export interface UserResponse {
  id: string;
  name: string;
  email: string | null;
  phone: string | null;
  token_balance: number;
  is_active: boolean;
  role: UserRole;
  team_id: string | null;
  team_role: TeamRole | null;
  meal_allowance: number;
  meals_balance: number;
}

export interface TokenPair {
  access_token: string;
  refresh_token: string;
  expires_in: number;
  role: UserRole;
  user_id: string;
  name: string;
  team_id: string | null;
  team_role: TeamRole | null;
  permissions: string[];
}

export interface CardResponse {
  card_uid: string;
  user: UserResponse;
}

export interface TeamResponse {
  id: string;
  name: string;
  capacity: number;
  current_size: number;
}

export interface MealSwipeResponse {
  id: string;
  user_id: string;
  meal_type: MealType;
  swipe_timestamp: string;
  meals_remaining?: number;
}

export interface TransactionResponse {
  id: string;
  user_id: string;
  amount: number;
  transaction_type: TransactionType;
  description: string | null;
  created_at: string;
}

export interface HallResponse {
  id: string;
  name: string;
  capacity_threshold: number;
  allowed_roles: UserRole[];
  current_occupancy: number;
}

export interface HallAvailability {
  id: string;
  name: string;
  current_occupancy: number;
  capacity_threshold: number;
  state: HallState;
}

export interface AccessLogResponse {
  id: string;
  user_id: string;
  hall_id: string;
  action: AccessAction;
  timestamp: string;
  allowed: boolean;
  reason: string | null;
}

export interface NotificationResponse {
  id: string;
  sender_id: string;
  sender_name?: string | null;
  audience: BroadcastAudience;
  team_id?: string | null;
  team_name?: string | null;
  recipient_id?: string | null;
  recipient_name?: string | null;
  message: string;
  timestamp: string;
  recipient_count: number;
}

export interface DashboardOverview {
  total_meals_today: number;
  total_access_scans_today: number;
  active_users: number;
  people_inside_halls: number;
}

export interface DelegateLocation {
  user_id: string;
  name: string;
  role: string;
  team_id: string | null;
  team_name: string | null;
  card_uid: string | null;
  current_hall_id: string | null;
  current_hall_name: string | null;
  status: 'inside' | 'exited' | 'never_scanned';
  last_action: string | null;
  last_seen: string | null;
}

export interface LiveScanEvent {
  id: string;
  scan_type: 'access_entry' | 'access_exit' | 'meal_swipe';
  delegate_id: string;
  delegate_name: string;
  delegate_role: string;
  team_name: string | null;
  location_or_service: string;
  allowed: boolean;
  reason: string | null;
  timestamp: string;
  meals_remaining?: number | null;
}

export interface ReportSummary {
  total_records: number;
  generated_at: string;
}

export interface AnalyticsSummary {
  metric: string;
  total: number;
  generated_at: string;
}
