import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {AuthService} from './core/auth/auth-service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected title = 'stockpulse';

  constructor(private authService: AuthService) {
    this.authService.checkAuthenticationStatus();
  }
}
