import { Component } from '@angular/core';
import { HeaderComponent } from '../../../shared/header-component/header-component';

@Component({
  selector: 'app-dashboard-component',
  imports: [HeaderComponent],
  templateUrl: './dashboard-component.html',
  styleUrl: './dashboard-component.css'
})
export class DashboardComponent {

  getFirstName(): string {
    return sessionStorage.getItem("firstName") || "";
  }

}
