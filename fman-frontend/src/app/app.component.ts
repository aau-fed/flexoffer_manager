import { Component, OnInit, ViewChild, OnDestroy } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';

import { Idle, DEFAULT_INTERRUPTSOURCES } from '@ng-idle/core';
import { Keepalive } from '@ng-idle/keepalive';

import { UserService } from './shared';
import { User } from './shared/models';


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit, OnDestroy {

  loggedIn = false;
  currentUser: User;

  idleState = 'Not started';
  timedOut = false;
  idleTimeoutInterval: number = 30 * 60; // 30 min
  countdownInterval: number = 1 * 60; // 1 min
  pingInterval: number = 30 * 60; // 30 min
  IdleAlreadyRunning: boolean = false;

  constructor(
    private userService: UserService,
    private router: Router,
    private idle: Idle,
    private keepalive: Keepalive
  ) {

    if (!this.userService.isLoggedIn()) {
      this.loggedIn = false;
      this.router.navigateByUrl('/login');
    } else {
      this.loggedIn = true;
    }

  }

  ngOnInit() {

    // set an idle timeout
    this.idle.setIdle(this.idleTimeoutInterval);
    // sets a timeout period after which the user will be logged out
    this.idle.setTimeout(this.countdownInterval);
    // sets the default interrupts, in this case, things like clicks, scrolls, touches to the document
    this.idle.setInterrupts(DEFAULT_INTERRUPTSOURCES);

    this.idle.onIdleEnd.subscribe(() => {
      this.idleState = 'No longer idle.';
      console.log(this.idleState);
    });

    this.idle.onTimeout.subscribe(() => {
      this.idleState = 'Timed out!';
      this.timedOut = true;
      console.log(this.idleState);
      this.IdleAlreadyRunning = false;
      this.userService.purgeAuth();
      this.router.navigateByUrl('/login');
    });

    this.idle.onIdleStart.subscribe(() => {
      this.idleState = 'You\'ve gone idle!';
      console.log(this.idleState);
    });

    this.idle.onTimeoutWarning.subscribe((countdown) => {
      this.idleState = 'You will be logged out in ' + countdown + ' seconds!';
      console.log(this.idleState);
    });

    // sets the ping interval
    this.keepalive.interval(this.pingInterval);
    this.keepalive.onPing.subscribe(() => {
      console.log("Refreshing token...");
      this.userService.refreshToken()
        .subscribe(
          (data) => {
            console.log("successfully refreshed token");
          },
          (err) => {
            console.log("failed to refresh token");
          }
        );
      // this.lastPing = new Date();
    });

    this.router.events
      .filter(event => event instanceof NavigationEnd)
      .subscribe(event => {
        // console.log(event.url);
        if ('url' in event && event.url !== '/login' && !this.IdleAlreadyRunning) {
          this.reset();
          this.IdleAlreadyRunning = true;
        }
      });
  }

  reset() {
    this.idle.watch(false);
    this.idleState = 'Idle watch started.';
    this.timedOut = false;
    // console.log(this.idleState);
  }

  ngOnDestroy() {
    this.idle.stop()
  }
}
