import Foundation

public final class APIService {
    public static let shared = APIService()
    private let client = APIClient.shared
    private let encoder = JSONEncoder()
    
    private init() {}
    
    // MARK: - Health
    public func checkHealth() async throws -> [String: String] {
        try await client.request(endpoint: "/health", method: "GET")
    }
    
    // MARK: - Auth
    public func login(request: LoginRequest) async throws -> TokenPair {
        let body = try encoder.encode(request)
        return try await client.request(endpoint: "auth/login", method: "POST", body: body)
    }
    
    public func register(request: RegisterRequest) async throws -> TokenPair {
        let body = try encoder.encode(request)
        return try await client.request(endpoint: "auth/register", method: "POST", body: body)
    }
    
    // MARK: - Dashboard
    public func getDashboardOverview(token: String) async throws -> DashboardOverview {
        try await client.request(endpoint: "dashboard/overview", method: "GET", token: token)
    }
    
    // MARK: - Users
    public func getUsers(token: String) async throws -> [UserResponse] {
        try await client.request(endpoint: "users", method: "GET", token: token)
    }
    
    public func createUser(user: UserCreate, token: String) async throws -> UserResponse {
        let body = try encoder.encode(user)
        return try await client.request(endpoint: "users", method: "POST", body: body, token: token)
    }
    
    public func getUser(userId: String, token: String) async throws -> UserResponse {
        try await client.request(endpoint: "users/\(userId)", method: "GET", token: token)
    }
    
    public func updateUser(userId: String, update: UserUpdate, token: String) async throws -> UserResponse {
        let body = try encoder.encode(update)
        return try await client.request(endpoint: "users/\(userId)", method: "PUT", body: body, token: token)
    }
    
    public func deleteUser(userId: String, token: String) async throws -> EmptyResponse {
        try await client.request(endpoint: "users/\(userId)", method: "DELETE", token: token)
    }
    
    public func adjustUserMeals(userId: String, body: MealAdjustRequest, token: String) async throws -> UserResponse {
        let reqBody = try encoder.encode(body)
        return try await client.request(endpoint: "users/\(userId)/meals/adjust", method: "POST", body: reqBody, token: token)
    }
    
    public func linkCard(request: CardLinkRequest, token: String) async throws -> CardResponse {
        let body = try encoder.encode(request)
        return try await client.request(endpoint: "users/cards/link", method: "POST", body: body, token: token)
    }
    
    public func getCard(uid: String, token: String) async throws -> CardResponse {
        try await client.request(endpoint: "users/cards/\(uid)", method: "GET", token: token)
    }
    
    // MARK: - Meals
    public func swipeMeal(request: MealSwipeRequest, token: String) async throws -> MealSwipeResponse {
        let body = try encoder.encode(request)
        return try await client.request(endpoint: "meals/swipe", method: "POST", body: body, token: token)
    }
    
    // MARK: - Attendance
    public func getAttendance(token: String) async throws -> AttendanceResponse {
        try await client.request(endpoint: "attendance", method: "GET", token: token)
    }
    
    public func recordAttendanceSwipe(request: AttendanceSwipeRequest, token: String) async throws -> AttendanceSwipeResponse {
        let body = try encoder.encode(request)
        return try await client.request(endpoint: "attendance/swipe", method: "POST", body: body, token: token)
    }
    
    public func updateSingleAttendance(request: SingleAttendanceRequest, token: String) async throws -> SingleAttendanceResponse {
        let body = try encoder.encode(request)
        return try await client.request(endpoint: "attendance/single", method: "POST", body: body, token: token)
    }
    
    public func updateBulkAttendance(request: BulkAttendanceRequest, token: String) async throws -> BulkAttendanceResponse {
        let body = try encoder.encode(request)
        return try await client.request(endpoint: "attendance/bulk", method: "POST", body: body, token: token)
    }
    
    // MARK: - Locations & Operations
    public func getLocations(token: String) async throws -> [DelegateLocation] {
        try await client.request(endpoint: "operations/locations", method: "GET", token: token)
    }
    
    public func getLiveScans(type: String? = nil, token: String) async throws -> [LiveScanEvent] {
        var queryItems: [URLQueryItem]? = nil
        if let type = type {
            queryItems = [URLQueryItem(name: "type", value: type)]
        }
        return try await client.request(endpoint: "operations/scans", method: "GET", token: token, queryItems: queryItems)
    }
    
    // MARK: - Access Control & Halls
    public func getHalls(token: String) async throws -> [HallResponse] {
        try await client.request(endpoint: "access/halls", method: "GET", token: token)
    }
    
    public func getHallAvailability(token: String) async throws -> [HallAvailability] {
        try await client.request(endpoint: "access/halls/availability", method: "GET", token: token)
    }
    
    public func createHall(hall: HallCreate, token: String) async throws -> HallResponse {
        let body = try encoder.encode(hall)
        return try await client.request(endpoint: "access/halls", method: "POST", body: body, token: token)
    }
    
    public func updateHall(hallId: String, update: HallUpdate, token: String) async throws -> HallResponse {
        let body = try encoder.encode(update)
        return try await client.request(endpoint: "access/halls/\(hallId)", method: "PUT", body: body, token: token)
    }
    
    public func getHallPresence(hallId: String, token: String) async throws -> [String] {
        try await client.request(endpoint: "access/halls/\(hallId)/presence", method: "GET", token: token)
    }
    
    public func scanAccess(request: AccessScanRequest, token: String) async throws -> AccessLogResponse {
        let body = try encoder.encode(request)
        return try await client.request(endpoint: "access/scan", method: "POST", body: body, token: token)
    }
    
    // MARK: - Teams
    public func getTeams(token: String) async throws -> [TeamResponse] {
        try await client.request(endpoint: "teams", method: "GET", token: token)
    }
    
    public func createTeam(team: TeamCreate, token: String) async throws -> TeamResponse {
        let body = try encoder.encode(team)
        return try await client.request(endpoint: "teams", method: "POST", body: body, token: token)
    }
    
    public func updateTeam(teamId: String, update: TeamUpdate, token: String) async throws -> TeamResponse {
        let body = try encoder.encode(update)
        return try await client.request(endpoint: "teams/\(teamId)", method: "PUT", body: body, token: token)
    }
    
    public func deleteTeam(teamId: String, token: String) async throws -> EmptyResponse {
        try await client.request(endpoint: "teams/\(teamId)", method: "DELETE", token: token)
    }
    
    public func assignMember(teamId: String, userId: String, role: TeamRole, token: String) async throws -> [String: String] {
        let query = [URLQueryItem(name: "role", value: role.rawValue)]
        return try await client.request(endpoint: "teams/\(teamId)/members/\(userId)", method: "POST", token: token, queryItems: query)
    }
    
    public func removeMember(teamId: String, userId: String, token: String) async throws -> EmptyResponse {
        try await client.request(endpoint: "teams/\(teamId)/members/\(userId)", method: "DELETE", token: token)
    }
    
    public func getTeamMembers(teamId: String, token: String) async throws -> [UserResponse] {
        try await client.request(endpoint: "teams/\(teamId)/members", method: "GET", token: token)
    }
    
    // MARK: - Notifications & Broadcasts
    public func broadcast(senderId: String, body: BroadcastCreate, token: String) async throws -> NotificationResponse {
        let reqBody = try encoder.encode(body)
        let query = [URLQueryItem(name: "sender_id", value: senderId)]
        return try await client.request(endpoint: "notifications", method: "POST", body: reqBody, token: token, queryItems: query)
    }
    
    public func getNotifications(limit: Int = 50, token: String) async throws -> [NotificationResponse] {
        let query = [URLQueryItem(name: "limit", value: "\(limit)")]
        return try await client.request(endpoint: "notifications", method: "GET", token: token, queryItems: query)
    }
    
    public func getInbox(userId: String, token: String) async throws -> [NotificationResponse] {
        try await client.request(endpoint: "notifications/inbox/\(userId)", method: "GET", token: token)
    }
    
    // MARK: - Analytics & Reports
    public func getDailyAnalytics(token: String) async throws -> AnalyticsSummary {
        try await client.request(endpoint: "analytics/daily", method: "GET", token: token)
    }
    
    public func getTrendAnalytics(token: String) async throws -> AnalyticsSummary {
        try await client.request(endpoint: "analytics/trends", method: "GET", token: token)
    }
    
    public func getMealsReport(token: String) async throws -> ReportSummary {
        try await client.request(endpoint: "reports/meals", method: "GET", token: token)
    }
    
    public func getTransactionsReport(token: String) async throws -> ReportSummary {
        try await client.request(endpoint: "reports/transactions", method: "GET", token: token)
    }
    
    public func getUsersReport(token: String) async throws -> ReportSummary {
        try await client.request(endpoint: "reports/users", method: "GET", token: token)
    }
    
    public func exportReport(token: String) async throws -> [String: String] {
        try await client.request(endpoint: "reports/export", method: "GET", token: token)
    }
    
    // MARK: - Settings
    public func updateMealWindows(request: MealWindowRequest, token: String) async throws -> [String: MealWindowRequest] {
        let body = try encoder.encode(request)
        return try await client.request(endpoint: "settings/meal-windows", method: "PUT", body: body, token: token)
    }
}
