import { FMANApiService } from "../shared/services/fmanapi.service";
import {
  Component,
  OnInit,
  OnDestroy,
  AfterViewInit,
  ViewChild,
  ElementRef,
  HostListener,
  ViewContainerRef
} from "@angular/core";
import { Router } from "@angular/router";

import { UserService } from "../shared/services";
import { DialogMeta } from "../shared/models/dialog-meta.model";
import { PageChangedEvent } from "ngx-bootstrap/pagination";
import { FlexofferModalService } from "../shared/helpers/flexoffermodal.service";
import { AlertService } from "../shared/services/alert.service";

declare var $: any;

@Component({
  selector: "app-trading",
  templateUrl: "./trading.component.html"
})
export class TradingComponent implements OnInit, OnDestroy {
  loggedIn = false;
  data: any[] = [];
  tradingState: number = 0;
  activePortfolio: any = null;
  activeTransaction: any = null;
  activeTransactionOffers: any[] = null;
  intervalId = null;

  @ViewChild("chartContainer")
  private chartContainer: ElementRef;

  constructor(
    private router: Router,
    private userService: UserService,
    private fmanApiService: FMANApiService,
    private foModalService: FlexofferModalService,
    private alertService: AlertService
  ) {}

  ngOnInit() {
    if (!this.userService.isLoggedIn()) {
      this.router.navigateByUrl("/login");
    }

    this.checkStatus();

    this.fmanApiService.getActivePortfolio().subscribe(res => {
      this.activePortfolio = res;
    });

    this.fmanApiService.getActiveTransaction().subscribe(res => {
      this.activeTransaction = res;
      this.activeTransactionOffers =
        res != null && res.tradingFlexOffer ? [res.tradingFlexOffer] : null;
    });

    const self = this;
    this.intervalId = setInterval(() => {
      self.checkStatus();
    }, 10000);
  }

  ngOnDestroy() {
    clearInterval(this.intervalId);
  }

  checkStatus() {
    this.fmanApiService.getTradingState().subscribe(state => {
      this.tradingState = state;
    });
  }

  showOffer(h) {
    if (h && h.tradingFlexOffer != null) {
      this.foModalService.showFoVisualization(h.tradingFlexOffer);
    }
  }

  sendOffer(event) {
    this.fmanApiService.sendUpdateFMARbid().subscribe(data => {
      this.alertService.alert("FMAR bid update succesfully tiggered.");
      this.ngOnInit();
    });
  }
}
