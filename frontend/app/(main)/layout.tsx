export default function MainLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="mx-auto max-w-[1400px] pt-32 pb-12 px-4 md:px-8">
      {children}
    </div>
  );
}
