import SwiftUI

// MARK: - Pressable Button Style with Tactile Haptics
public struct MianuPressableButtonStyle: ButtonStyle {
    public let scale: CGFloat
    public init(scale: CGFloat = 0.97) {
        self.scale = scale
    }
    
    public func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? scale : 1.0)
            .opacity(configuration.isPressed ? 0.92 : 1.0)
            .animation(.easeOut(duration: 0.12), value: configuration.isPressed)
            .onChange(of: configuration.isPressed) { pressed in
                if pressed {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                }
            }
    }
}

// MARK: - Primary Button
public struct MianuButton: View {
    public let title: String
    public let icon: String?
    public let isLoading: Bool
    public let isEnabled: Bool
    public let containerColor: Color
    public let contentColor: Color
    public let action: () -> Void
    
    public init(
        _ title: String,
        icon: String? = nil,
        isLoading: Bool = false,
        isEnabled: Bool = true,
        containerColor: Color = .white,
        contentColor: Color = .black,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.icon = icon
        self.isLoading = isLoading
        self.isEnabled = isEnabled
        self.containerColor = containerColor
        self.contentColor = contentColor
        self.action = action
    }
    
    public var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                if isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: contentColor))
                        .scaleEffect(0.85)
                } else {
                    if let icon = icon {
                        Image(systemName: icon)
                            .font(.system(size: 15, weight: .semibold))
                    }
                    Text(title)
                        .font(.system(size: 15, weight: .bold))
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: 50)
            .background(isEnabled ? containerColor : Color(hex: 0x1A1E29))
            .foregroundColor(isEnabled ? contentColor : MianuColors.fgMuted)
            .clipShape(MianuShapes.medium)
        }
        .buttonStyle(MianuPressableButtonStyle(scale: 0.98))
        .disabled(!isEnabled || isLoading)
    }
}

// MARK: - Secondary Button
public struct MianuSecondaryButton: View {
    public let title: String
    public let icon: String?
    public let tint: Color
    public let height: CGFloat
    public let isEnabled: Bool
    public let action: () -> Void
    
    public init(
        _ title: String,
        icon: String? = nil,
        tint: Color = .white,
        height: CGFloat = 42,
        isEnabled: Bool = true,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.icon = icon
        self.tint = tint
        self.height = height
        self.isEnabled = isEnabled
        self.action = action
    }
    
    public var body: some View {
        Button(action: action) {
            HStack(spacing: 6) {
                if let icon = icon {
                    Image(systemName: icon)
                        .font(.system(size: 13, weight: .semibold))
                }
                Text(title)
                    .font(.system(size: 13, weight: .semibold))
            }
            .padding(.horizontal, 14)
            .frame(height: height)
            .background(Color(hex: 0x14171E))
            .foregroundColor(isEnabled ? tint : MianuColors.fgMuted)
            .overlay(
                RoundedRectangle(cornerRadius: 10, style: .continuous)
                    .stroke(MianuColors.border, lineWidth: 1)
            )
            .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
        }
        .buttonStyle(MianuPressableButtonStyle(scale: 0.96))
        .disabled(!isEnabled)
    }
}

// MARK: - Text Field
public struct MianuTextField: View {
    public let label: String
    public let placeholder: String
    public let leadingIcon: String?
    @Binding public var text: String
    public var isSecure: Bool = false
    
    public init(
        _ label: String,
        placeholder: String = "",
        leadingIcon: String? = nil,
        text: Binding<String>,
        isSecure: Bool = false
    ) {
        self.label = label
        self.placeholder = placeholder
        self.leadingIcon = leadingIcon
        self._text = text
        self.isSecure = isSecure
    }
    
    public var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.system(size: 12, weight: .medium))
                .foregroundColor(MianuColors.fgMuted)
            
            HStack(spacing: 10) {
                if let icon = leadingIcon {
                    Image(systemName: icon)
                        .font(.system(size: 14))
                        .foregroundColor(MianuColors.fgMuted)
                }
                
                if isSecure {
                    SecureField(placeholder, text: $text)
                        .font(.system(size: 14))
                        .foregroundColor(.white)
                } else {
                    TextField(placeholder, text: $text)
                        .font(.system(size: 14))
                        .foregroundColor(.white)
                        .autocapitalization(.none)
                }
            }
            .padding(.horizontal, 14)
            .frame(height: 48)
            .background(Color(hex: 0x12151D))
            .overlay(
                RoundedRectangle(cornerRadius: 12, style: .continuous)
                    .stroke(MianuColors.border, lineWidth: 1)
            )
            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
        }
    }
}

// MARK: - Telemetry Stat Card
public struct StatCard: View {
    public let title: String
    public let value: String
    public let caption: String
    public let icon: String
    public var accentColor: Color = .white
    
    public init(
        title: String,
        value: String,
        caption: String,
        icon: String,
        accentColor: Color = .white
    ) {
        self.title = title
        self.value = value
        self.caption = caption
        self.icon = icon
        self.accentColor = accentColor
    }
    
    public var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(title.uppercased())
                    .font(.system(size: 10, weight: .semibold, design: .monospaced))
                    .foregroundColor(MianuColors.fgMuted)
                Spacer()
                Image(systemName: icon)
                    .font(.system(size: 14))
                    .foregroundColor(accentColor)
            }
            
            Text(value)
                .font(.system(size: 22, weight: .bold, design: .rounded))
                .foregroundColor(.white)
            
            Text(caption)
                .font(.system(size: 11))
                .foregroundColor(MianuColors.fgMuted)
                .lineLimit(1)
        }
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .glassCard()
    }
}

// MARK: - Status Pill
public struct StatusPill: View {
    public let text: String
    public let color: Color
    
    public init(text: String, color: Color = MianuColors.success) {
        self.text = text
        self.color = color
    }
    
    public var body: some View {
        Text(text.uppercased())
            .font(.system(size: 10, weight: .bold, design: .monospaced))
            .foregroundColor(color)
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(color.opacity(0.12))
            .overlay(
                Capsule(style: .continuous)
                    .stroke(color.opacity(0.4), lineWidth: 1)
            )
            .clipShape(Capsule(style: .continuous))
    }
}

// MARK: - Avatar View
public struct AvatarView: View {
    public let initials: String
    public var size: CGFloat = 44
    public var presenceColor: Color? = nil
    
    public init(initials: String, size: CGFloat = 44, presenceColor: Color? = nil) {
        self.initials = initials
        self.size = size
        self.presenceColor = presenceColor
    }
    
    public var body: some View {
        ZStack(alignment: .bottomTrailing) {
            Circle()
                .fill(Color(hex: 0x1A1F2C))
                .overlay(
                    Circle()
                        .stroke(MianuColors.borderBright, lineWidth: 1)
                )
                .frame(width: size, height: size)
                .overlay(
                    Text(initials)
                        .font(.system(size: size * 0.38, weight: .bold))
                        .foregroundColor(.white)
                )
            
            if let dot = presenceColor {
                Circle()
                    .fill(dot)
                    .frame(width: size * 0.28, height: size * 0.28)
                    .overlay(Circle().stroke(Color.black, lineWidth: 2))
            }
        }
    }
}

// MARK: - Filter Pill
public struct FilterPill: View {
    public let title: String
    public let isSelected: Bool
    public let action: () -> Void
    
    public init(title: String, isSelected: Bool, action: @escaping () -> Void) {
        self.title = title
        self.isSelected = isSelected
        self.action = action
    }
    
    public var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 12, weight: isSelected ? .bold : .medium))
                .foregroundColor(isSelected ? .black : MianuColors.fgMuted)
                .padding(.horizontal, 14)
                .padding(.vertical, 7)
                .background(isSelected ? Color.white : Color(hex: 0x14171E))
                .overlay(
                    Capsule(style: .continuous)
                        .stroke(isSelected ? Color.white : MianuColors.border, lineWidth: 1)
                )
                .clipShape(Capsule(style: .continuous))
        }
        .buttonStyle(MianuPressableButtonStyle(scale: 0.95))
    }
}

// MARK: - Section Header
public struct SectionHeader: View {
    public let title: String
    public var eyebrow: String = "MIANU-SM"
    public var subtitle: String? = nil
    
    public init(title: String, eyebrow: String = "MIANU-SM", subtitle: String? = nil) {
        self.title = title
        self.eyebrow = eyebrow
        self.subtitle = subtitle
    }
    
    public var body: some View {
        VStack(alignment: .leading, spacing: 3) {
            Text(eyebrow.uppercased())
                .font(.system(size: 11, weight: .bold, design: .monospaced))
                .foregroundColor(MianuColors.fgMuted)
            
            Text(title)
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(.white)
            
            if let sub = subtitle {
                Text(sub)
                    .font(.system(size: 13))
                    .foregroundColor(MianuColors.fgMuted)
            }
        }
    }
}

// MARK: - Empty State View
public struct EmptyStateView: View {
    public let title: String
    public let description: String
    public let icon: String
    public var buttonTitle: String? = nil
    public var buttonAction: (() -> Void)? = nil
    
    public init(
        title: String,
        description: String,
        icon: String = "tray.fill",
        buttonTitle: String? = nil,
        buttonAction: (() -> Void)? = nil
    ) {
        self.title = title
        self.description = description
        self.icon = icon
        self.buttonTitle = buttonTitle
        self.buttonAction = buttonAction
    }
    
    public var body: some View {
        VStack(spacing: 14) {
            Image(systemName: icon)
                .font(.system(size: 36))
                .foregroundColor(MianuColors.fgMuted)
            
            Text(title)
                .font(.system(size: 17, weight: .semibold))
                .foregroundColor(.white)
            
            Text(description)
                .font(.system(size: 13))
                .foregroundColor(MianuColors.fgMuted)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 24)
            
            if let btn = buttonTitle, let action = buttonAction {
                Button(action: action) {
                    Text(btn)
                        .font(.system(size: 13, weight: .bold))
                        .foregroundColor(.black)
                        .padding(.horizontal, 20)
                        .padding(.vertical, 9)
                        .background(Color.white)
                        .clipShape(Capsule(style: .continuous))
                }
                .buttonStyle(MianuPressableButtonStyle(scale: 0.96))
                .padding(.top, 4)
            }
        }
        .padding(32)
        .frame(maxWidth: .infinity)
    }
}
