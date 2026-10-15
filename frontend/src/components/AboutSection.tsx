interface AboutSectionProps {
  heading: string
  body: string
  imageSrc: string
}

// A blank line in the text starts a new paragraph
function toParagraphs(body: string): string[] {
  return body
    .split(/\n\s*\n/)
    .map((p) => p.trim())
    .filter((p) => p.length > 0)
}

// The "Om oss" box. Used by the website and by the admin preview, so the preview is exactly what visitors see.
export function AboutSection({ heading, body, imageSrc }: AboutSectionProps) {
  return (
    <div className="about-card">
      <div className="about-text">
        <h3>{heading}</h3>
        {toParagraphs(body).map((paragraph, index) => (
          <p key={index}>{paragraph}</p>
        ))}
      </div>
      <div className="about-image">
        <img src={imageSrc} alt="Arbeidsplassen hos The Beauty Room by AG" loading="lazy" />
      </div>
    </div>
  )
}
