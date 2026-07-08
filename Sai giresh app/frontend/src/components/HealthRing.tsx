"use client"

interface HealthRingProps {
  score: number
  size?: number
  strokeWidth?: number
}

export default function HealthRing({ score, size = 160, strokeWidth = 10 }: HealthRingProps) {
  const radius = (size - strokeWidth) / 2
  const circumference = 2 * Math.PI * radius
  const offset = circumference - (score / 100) * circumference

  const getColor = (s: number) => {
    if (s >= 70) return "#34d399"
    if (s >= 40) return "#fbbf24"
    return "#f87171"
  }

  const getLabel = (s: number) => {
    if (s >= 70) return "Excellent"
    if (s >= 40) return "Fair"
    return "Needs Work"
  }

  return (
    <div className="health-ring" style={{ width: size, height: size }}>
      <svg width={size} height={size} className="-rotate-90">
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke="rgba(255,255,255,0.06)"
          strokeWidth={strokeWidth}
        />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke={getColor(score)}
          strokeWidth={strokeWidth}
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={offset}
          style={{ transition: "stroke-dashoffset 1s ease" }}
        />
      </svg>
      <div className="absolute inset-0 flex flex-col items-center justify-center">
        <span className="text-4xl font-bold" style={{ color: getColor(score) }}>{score}</span>
        <span className="text-xs text-gray-400 mt-1">{getLabel(score)}</span>
      </div>
    </div>
  )
}
