import Foundation

public enum APIError: LocalizedError {
    case invalidURL
    case networkError(Error)
    case serverError(statusCode: Int, message: String)
    case decodingError(Error)
    case unauthorized
    case unknown
    
    public var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "Invalid endpoint URL"
        case .networkError(let err):
            return err.localizedDescription
        case .serverError(let code, let msg):
            return "Server Error (\(code)): \(msg)"
        case .decodingError(let err):
            return "Failed to parse response: \(err.localizedDescription)"
        case .unauthorized:
            return "Session expired or invalid credentials. Please log in again."
        case .unknown:
            return "An unexpected error occurred."
        }
    }
}

public actor APIClient {
    public static let shared = APIClient()
    
    private let session: URLSession
    private let jsonDecoder: JSONDecoder
    private let jsonEncoder: JSONEncoder
    
    private init() {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 25
        config.timeoutIntervalForResource = 60
        self.session = URLSession(configuration: config)
        
        self.jsonDecoder = JSONDecoder()
        self.jsonEncoder = JSONEncoder()
    }
    
    public func request<T: Decodable>(
        endpoint: String,
        method: String = "GET",
        body: Data? = nil,
        token: String? = nil,
        queryItems: [URLQueryItem]? = nil
    ) async throws -> T {
        let base = Config.shared.baseURL.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        let cleanEndpoint = endpoint.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        
        // Handle routes that belong to /api/v1 vs root
        let urlString: String
        if cleanEndpoint.hasPrefix("api/v1") || cleanEndpoint == "health" {
            urlString = "\(base)/\(cleanEndpoint)"
        } else {
            urlString = "\(base)/api/v1/\(cleanEndpoint)"
        }
        
        guard var components = URLComponents(string: urlString) else {
            throw APIError.invalidURL
        }
        
        if let query = queryItems, !query.isEmpty {
            components.queryItems = query
        }
        
        guard let url = components.url else {
            throw APIError.invalidURL
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        
        if let token = token, !token.isEmpty {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        
        if let body = body {
            request.httpBody = body
        }
        
        let data: Data
        let response: URLResponse
        do {
            (data, response) = try await session.data(for: request)
        } catch {
            throw APIError.networkError(error)
        }
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw APIError.unknown
        }
        
        if httpResponse.statusCode == 401 {
            throw APIError.unauthorized
        }
        
        guard (200...299).contains(httpResponse.statusCode) else {
            var errMsg = "HTTP \(httpResponse.statusCode)"
            if let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
                if let detail = json["detail"] as? String {
                    errMsg = detail
                } else if let msg = json["message"] as? String {
                    errMsg = msg
                } else if let err = json["error"] as? String {
                    errMsg = err
                }
            }
            throw APIError.serverError(statusCode: httpResponse.statusCode, message: errMsg)
        }
        
        // Special case for Void or Empty responses
        if T.self == EmptyResponse.self {
            return EmptyResponse() as! T
        }
        
        do {
            return try jsonDecoder.decode(T.self, from: data)
        } catch {
            throw APIError.decodingError(error)
        }
    }
}

public struct EmptyResponse: Codable {}
