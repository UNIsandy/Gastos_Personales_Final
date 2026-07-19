import { Component } from '@angular/core';
import { RouterOutlet, Router } from '@angular/router';
import { HeaderComponent } from './components/header/header';
import { FooterComponent } from './components/footer/footer';
import { SidebarComponent } from './components/sidebar/sidebar';
import { ToastContainerComponent } from './components/toast-container/toast-container';
import { ConfirmDialogComponent } from './components/confirm-dialog/confirm-dialog';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HeaderComponent, FooterComponent, SidebarComponent, ToastContainerComponent, ConfirmDialogComponent, CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  constructor(public router: Router) {}
}