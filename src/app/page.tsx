export default function Home() {
  return (
    <main className="page">
      <section className="hero">
        <span className="badge">
          <span className="badge-dot" aria-hidden="true" />
          Live
        </span>
        <h1 className="title">Kuklive</h1>
        <p className="subtitle">
          Go live, stay connected. A fresh Next.js foundation, ready to build on.
        </p>
        <div className="actions">
          <a
            className="button button-primary"
            href="https://nextjs.org/docs"
            target="_blank"
            rel="noopener noreferrer"
          >
            Read the docs
          </a>
          <a
            className="button button-ghost"
            href="https://github.com/amithkukllod777/Kuklive"
            target="_blank"
            rel="noopener noreferrer"
          >
            View on GitHub
          </a>
        </div>
      </section>
      <footer className="footer">
        Edit <code>src/app/page.tsx</code> to get started.
      </footer>
    </main>
  );
}
