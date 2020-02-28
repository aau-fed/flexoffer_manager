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
import { Router, ActivatedRoute } from "@angular/router";

import { UserService } from "../shared/services";
import { DialogMeta } from "../shared/models/dialog-meta.model";
import { PageChangedEvent } from "ngx-bootstrap/pagination";
import { FlexofferModalService } from "../shared/helpers/flexoffermodal.service";
import { AlertService } from "../shared/services/alert.service";
import { UserlistService } from "../userlist/userlist.service";
import { moment } from "ngx-bootstrap/chronos/test/chain";

declare var $: any;

@Component({
  selector: "app-user-bill",
  templateUrl: "./user-bill.component.html"
})
export class UserBillComponent implements OnInit {
  loggedIn = false;
  userName = "";
  bill_date = new Date();
  bill: any = {};

  @ViewChild("chartContainer")
  private chartContainer: ElementRef;

  constructor(
    private router: Router,
    private userService: UserService,
    private userlistService: UserlistService,
    private fmanApiService: FMANApiService,
    private foModalService: FlexofferModalService,
    private alertService: AlertService,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    if (!this.userService.isLoggedIn()) {
      this.router.navigateByUrl("/login");
    }

    this.userName = this.route.snapshot.paramMap.get("userName") || "";

    if (this.userName === "") {
      this.userService.currentUser.subscribe(userName => {
        this.userName = userName.userName;
        this.getBill();
      });
    } else {
      this.getBill();
    }
  }

  getBill() {
    this.userlistService
      .getBill(
        this.userName,
        moment(this.bill_date)
          .year()
          .toString() +
          "/" +
          moment(this.bill_date)
            .month()
            .toString()
      )
      .subscribe(bill => {
        this.bill = bill.data;
      });
  }
}
