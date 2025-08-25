import {Component, Input} from '@angular/core';
import {Router} from '@angular/router';
import {routes} from '../../app.routes';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-header-component',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header-component.html',
  styleUrl: './header-component.css'
})
export class HeaderComponent {

  @Input() firstName!: string;
  @Input() notificationCount: number = 0;

  constructor(private router: Router) {

  }

  onNotificationClick(): void {
    this.router.navigate([routes.find(route => route.path === 'notifications')?.path || '/notifications']);
  }

  onSettingsClick(): void {
    this.router.navigate([routes.find(route => route.path === 'settings')?.path || '/settings']);
  }

  onLogoClick(): void {
    this.router.navigate([routes.find(route => route.path === 'home')?.path || '/home']);
  }

  settingsEnabled(): boolean {
    return !!sessionStorage.getItem("isOAuthUser");
  }
}
