# UI/UX Design Notes

## Design Philosophy

Modern dark theme optimized for extended coding sessions. Prioritizes clarity, accessibility, and role-awareness.

## Color Palette

- **Primary**: `#0ea5e9` (Blue) - CTAs, links
- **Background**: `#020617` (Dark) - Main background
- **Cards**: `#1e293b` (Surface) - Panels, cards
- **Text**: `#f1f5f9` (Light) - Primary text

## Key Components

1. **Authentication**: Centered cards, gradient background
2. **Role Selection**: Dropdown + custom input, prominent CTA
3. **Dashboard**: Stats cards, role badge, topic coverage
4. **Interview**: Question display, answer input, AI feedback
5. **Coding**: Split view (problem | Monaco editor), run button
6. **Profile**: Role update section

## Responsive Design

- Mobile: Single column
- Tablet: 2-column layouts
- Desktop: 3-column stats, full features

## Accessibility

- Focus rings on all interactive elements
- WCAG AA color contrast
- Keyboard navigation support
- Semantic HTML

## Best Practices

- Consistent spacing (Tailwind scale)
- Smooth transitions (150ms)
- Loading states for all async operations
- Clear error messages
