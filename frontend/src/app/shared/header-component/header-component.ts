import {Component, Input} from '@angular/core';
import {Router} from '@angular/router';
import {routes} from '../../app.routes';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-header-component',
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
    console.log('Settings icon clicked');
    this.router.navigate([routes.find(route => route.path === 'settings')?.path || '/settings']);
  }
}
