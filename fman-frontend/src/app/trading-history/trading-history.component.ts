import { FMANApiService } from '../shared/services/fmanapi.service';
import { Component, OnInit, OnDestroy, AfterViewInit, ViewChild, ElementRef, HostListener, ViewContainerRef } from '@angular/core';
import { Router } from '@angular/router';


import { UserService } from '../shared/services';
import { DialogMeta } from '../shared/models/dialog-meta.model';
import { PageChangedEvent } from 'ngx-bootstrap/pagination';
import { FlexofferModalService } from '../shared/helpers/flexoffermodal.service';
import { moment } from 'ngx-bootstrap/chronos/test/chain';
import { formatNumber, DecimalPipe } from '@angular/common';

declare var $: any;


@Component({
  selector: 'app-trading-history',
  templateUrl: './trading-history.component.html',
  styleUrls: ['./trading-history.component.css'],
})
export class TradingHistoryComponent implements OnInit {
  loggedIn = false;
  pageSize = 10;
  currentPage = 0;
  datasetSize = 0;
  data: any[] = [];
  dateInterval = [moment().subtract(1, 'months').toDate(), moment().toDate()];
  commitments = [];

  @ViewChild('chartContainer')
  private chartContainer: ElementRef;

  constructor(
    private router: Router,
    private userService: UserService,
    private fmanApiService: FMANApiService,
    private foModalService: FlexofferModalService
  ) {}

  ngOnInit() {
    if (!this.userService.isLoggedIn()) {
      this.router.navigateByUrl('/login');
    }
    this.getPage(0);
  };

  public timePeriodChanged(event) {
    this.getPage(0);
  }

  private getPage(page: number) {
    this.currentPage = page;
    this.fmanApiService.getTradingHistory(page - 1, this.pageSize, this.dateInterval[0], this.dateInterval[1])
    .subscribe(data => {
        this.data = data.data || [];
        this.datasetSize = data.totalCount || this.data.length;
    })

    this.fmanApiService.getTradingCommitments(this.dateInterval[0], this.dateInterval[1])
    .subscribe(data => {
        this.commitments = data || [];
    })
  }

  pageChange(event: PageChangedEvent) {
    this.getPage(event.page);
  }

  showOffer(h) {
    if (h && h.tradingFlexOffer != null) {
        this.foModalService.showFoVisualization(h.tradingFlexOffer);
    }
  }


  public getCommitmentAmounts(c): String {
    const z = c.transaction.tradingFlexOffer.flexOfferSchedule.scheduleSlices.map(function(s, i) {
      return [(s.energyAmount - c.transaction.tradingFlexOffer.defaultSchedule.scheduleSlices[i].energyAmount).toFixed(2)];
    });

    return z.toString() + ' dkWh';
  }
}



