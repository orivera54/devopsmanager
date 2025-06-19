import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { FormsModule } from '@angular/forms'; // Import FormsModule
import { CommonModule } from '@angular/common'; // Import CommonModule for ngIf, ngFor etc.


@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  standalone: true, // Mark as standalone
  imports: [FormsModule, CommonModule] // Import necessary modules here for standalone
})
export class LoginComponent {
  loginData = {
    username: '',
    password: ''
  };
  errorMessage: string | null = null;

  constructor(private authService: AuthService, private router: Router) { }

  onSubmit(): void {
    if (!this.loginData.username || !this.loginData.password) {
        this.errorMessage = "Username and password are required.";
        return;
    }
    this.authService.login(this.loginData).subscribe({
      next: () => {
        // Navigate to a dashboard or home page upon successful login
        this.router.navigate(['/dashboard']); // Assume '/dashboard' is a protected route
        this.errorMessage = null;
      },
      error: (err) => {
        // Handle login errors (e.g., display an error message)
        console.error('Login failed:', err);
        if (err.status === 401) {
            this.errorMessage = 'Invalid username or password.';
        } else if (err.error && typeof err.error === 'string') {
             this.errorMessage = err.error;
        }
        else {
            this.errorMessage = 'Login failed. Please try again later.';
        }
      }
    });
  }
}
