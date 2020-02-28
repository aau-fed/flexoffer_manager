import { ModuleWithProviders, NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { CommonModule } from "@angular/common";

import { TradingComponent } from "./trading.component";

import { AuthGuard, SharedModule } from "../shared";
import { ChartModule } from "angular2-highcharts";
import { PaginationModule } from "ngx-bootstrap/pagination";
import { MomentModule } from "ngx-moment";

const tradingRouting: ModuleWithProviders = RouterModule.forChild([
  {
    path: "trading",
    component: TradingComponent
  }
]);

declare var require: any;

@NgModule({
  imports: [
    CommonModule,
    tradingRouting,
    SharedModule,
    MomentModule,
    PaginationModule.forRoot(),
    ChartModule.forRoot(require("highcharts"))
  ],
  declarations: [TradingComponent],
  providers: []
})
export class TradingModule {}
