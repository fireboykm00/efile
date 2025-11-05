import { cn } from "@/lib/utils";

interface LogoProps {
  size?: "sm" | "md" | "lg";
  showText?: boolean;
  className?: string;
  textClassName?: string;
}

export function Logo({ 
  size = "md", 
  showText = true, 
  className,
  textClassName 
}: LogoProps) {
  const sizeClasses = {
    sm: "h-6 w-6",
    md: "h-8 w-8", 
    lg: "h-12 w-12"
  };

  const textSizeClasses = {
    sm: "text-lg",
    md: "text-xl",
    lg: "text-2xl"
  };

  return (
    <div className={cn("flex items-center space-x-2", className)}>
      <img 
        src="/logo.png" 
        alt="E-FileConnect Logo" 
        className={cn(sizeClasses[size])}
      />
      {showText && (
        <span className={cn("font-bold", textSizeClasses[size], textClassName)}>
          E-FileConnect
        </span>
      )}
    </div>
  );
}
