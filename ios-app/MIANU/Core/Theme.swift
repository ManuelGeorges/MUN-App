import SwiftUI

// MARK: - Color Palette
public enum MianuColors {
    public static let bg = Color(hex: 0x08090C)
    public static let bgElevated = Color(hex: 0x0E1116)
    public static let bgSunken = Color(hex: 0x07080A)
    public static let surface = Color(hex: 0x14171E)
    public static let surfaceRaised = Color(hex: 0x1A1F29)
    
    public static let border = Color(hex: 0x222735)
    public static let borderBright = Color(hex: 0x323B4F)
    public static let borderSubtle = Color(hex: 0x181C26)
    
    public static let fg = Color(hex: 0xFFFFFF)
    public static let fgMuted = Color(hex: 0x94A3B8)
    public static let fgSubtle = Color(hex: 0x64748B)
    
    public static let accent = Color(hex: 0xFFFFFF)
    public static let success = Color(hex: 0x34D399) // Emerald
    public static let danger = Color(hex: 0xF87171)  // Crimson
    public static let warning = Color(hex: 0xFBBF24) // Amber
    public static let info = Color(hex: 0x60A5FA)    // Blue
}

// MARK: - Color Hex Extension
extension Color {
    init(hex: UInt32, alpha: Double = 1.0) {
        let red = Double((hex >> 16) & 0xFF) / 255.0
        let green = Double((hex >> 8) & 0xFF) / 255.0
        let blue = Double(hex & 0xFF) / 255.0
        self.init(.sRGB, red: red, green: green, blue: blue, opacity: alpha)
    }
}

// MARK: - Shape Constants
public enum MianuShapes {
    public static let small = RoundedRectangle(cornerRadius: 8, style: .continuous)
    public static let medium = RoundedRectangle(cornerRadius: 14, style: .continuous)
    public static let large = RoundedRectangle(cornerRadius: 20, style: .continuous)
    public static let pill = Capsule(style: .continuous)
}

// MARK: - Glassmorphism ViewModifiers
public struct GlassCardModifier: ViewModifier {
    var cornerRadius: CGFloat = 14
    var borderColor: Color = MianuColors.border
    
    public func body(content: Content) -> some View {
        content
            .background(
                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .fill(MianuColors.bgElevated.opacity(0.85))
                    .background(.ultraThinMaterial)
            )
            .overlay(
                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .stroke(borderColor, lineWidth: 1)
            )
            .clipShape(RoundedRectangle(cornerRadius: cornerRadius, style: .continuous))
    }
}

public struct GlassPanelModifier: ViewModifier {
    var cornerRadius: CGFloat = 18
    
    public func body(content: Content) -> some View {
        content
            .background(
                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .fill(
                        LinearGradient(
                            colors: [Color(hex: 0x141824).opacity(0.9), Color(hex: 0x0E1118).opacity(0.95)],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
            )
            .overlay(
                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .stroke(
                        LinearGradient(
                            colors: [MianuColors.borderBright.opacity(0.8), MianuColors.borderSubtle],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        ),
                        lineWidth: 1
                    )
            )
            .clipShape(RoundedRectangle(cornerRadius: cornerRadius, style: .continuous))
    }
}

extension View {
    public func glassCard(cornerRadius: CGFloat = 14, borderColor: Color = MianuColors.border) -> some View {
        self.modifier(GlassCardModifier(cornerRadius: cornerRadius, borderColor: borderColor))
    }
    
    public func glassPanel(cornerRadius: CGFloat = 18) -> some View {
        self.modifier(GlassPanelModifier(cornerRadius: cornerRadius))
    }
    
    public func ambientBackground() -> some View {
        self.background(
            ZStack {
                MianuColors.bg.ignoresSafeArea()
                RadialGradient(
                    colors: [Color(hex: 0x1A2234).opacity(0.25), Color.clear],
                    center: .top,
                    startRadius: 0,
                    endRadius: 500
                )
                .ignoresSafeArea()
            }
        )
    }
}
