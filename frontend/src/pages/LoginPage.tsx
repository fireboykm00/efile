import { useNavigate } from "react-router-dom";
import { LoginForm } from "@/components/auth/LoginForm";
import { FileText, Shield, Lock, Zap } from "lucide-react";

export function LoginPage() {
  const navigate = useNavigate();

  const handleLoginSuccess = () => {
    navigate("/dashboard");
  };

  return (
    <div className="min-h-screen flex bg-background">
      {/* Left Side - Branding */}
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-primary to-primary/80 p-12 flex-col justify-between text-primary-foreground">
        <div>
          <div className="flex items-center space-x-2">
            <FileText className="h-8 w-8" />
            <span className="text-2xl font-bold">E-FileConnect</span>
          </div>
          <p className="mt-4 text-lg opacity-90">
            Secure Document Management for Legal & Corporate Excellence
          </p>
        </div>

        <div className="space-y-6">
          <div className="flex items-start space-x-4">
            <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-primary-foreground/10">
              <Shield className="h-6 w-6" />
            </div>
            <div>
              <h3 className="font-semibold">Enterprise Security</h3>
              <p className="text-sm opacity-80">
                Bank-level encryption and role-based access control
              </p>
            </div>
          </div>

          <div className="flex items-start space-x-4">
            <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-primary-foreground/10">
              <Lock className="h-6 w-6" />
            </div>
            <div>
              <h3 className="font-semibold">Audit Trail</h3>
              <p className="text-sm opacity-80">
                Complete tracking of all document activities
              </p>
            </div>
          </div>

          <div className="flex items-start space-x-4">
            <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-primary-foreground/10">
              <Zap className="h-6 w-6" />
            </div>
            <div>
              <h3 className="font-semibold">Fast & Efficient</h3>
              <p className="text-sm opacity-80">
                Streamlined workflows for maximum productivity
              </p>
            </div>
          </div>
        </div>

        <div className="text-sm opacity-75">
          © 2024 E-FileConnect. All rights reserved.
        </div>
      </div>

      {/* Right Side - Login Form */}
      <div className="flex-1 flex items-center justify-center p-8">
        <div className="w-full max-w-md">
          {/* Mobile Logo */}
          <div className="lg:hidden mb-8 text-center">
            <div className="flex items-center justify-center space-x-2 mb-2">
              <FileText className="h-8 w-8 text-primary" />
              <span className="text-2xl font-bold">E-FileConnect</span>
            </div>
            <p className="text-sm text-muted-foreground">
              Secure Document Management System
            </p>
          </div>

          <LoginForm onLoginSuccess={handleLoginSuccess} />
        </div>
      </div>
    </div>
  );
}
