import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Kuklive",
  description: "Kuklive — go live, stay connected.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
