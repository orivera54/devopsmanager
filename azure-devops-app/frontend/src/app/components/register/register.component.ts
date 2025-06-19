import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { FormsModule } from '@angular/forms'; // Import FormsModule
import { CommonModule } from '@angular/common'; // Import CommonModule

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
  standalone: true, // Mark as standalone
  imports: [FormsModule, CommonModule] // Import necessary modules here
})
export class RegisterComponent {
  registerData = {
    username: '',
    email: '',
    password: ''
  };
  errorMessage: string | null = null;
  successMessage: string | null = null;

  constructor(private authService: AuthService, private router: Router) { }

  onSubmit(): void {
    if (!this.registerData.username || !this.registerData.email || !this.registerData.password) {
        this.errorMessage = "All fields are required.";
        return;
    }
    // Basic email validation
    if (!this.registerData.email.includes('@')) {
        this.errorMessage = "Please enter a valid email address.";
        return;
    }

    this.authService.register(this.registerData).subscribe({
      next: (response) => {
        this.successMessage = 'Registration successful! Please login.';
        this.errorMessage = null;
        // Optionally redirect to login page after a delay or on button click
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000); // Redirect after 2 seconds
      },
      error: (err) => {
        console.error('Registration failed:', err);
        if (err.status === 400 && err.error && err.error.error) {
             this.errorMessage = err.error.error; // Use error message from backend if available
        } else if (err.error && typeof err.error === 'string' && err.error.includes("User already exists")) {
            this.errorMessage = "User with this username or email already exists.";
        }
         else {
            this.errorMessage = 'Registration failed. Please try again later.';
        }
        this.successMessage = null;
      }
    });
  }
}
