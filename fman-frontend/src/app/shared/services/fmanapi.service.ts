import { Injectable } from "@angular/core";
import { URLSearchParams, Headers } from "@angular/http";
import { Observable } from "rxjs/Rx";
import "rxjs/add/operator/map";
import "rxjs/add/operator/catch";

import { ApiService } from "./api.service";
import { retry } from "rxjs-compat/operator/retry";

@Injectable()
export class FMANApiService {
  constructor(private apiService: ApiService) {}

  getDashboardContent(): Observable<any> {
    return this.apiService.get("/dashboard/").map(data => {
      return data;
    });
  }

  getDashboardConfig(): Observable<any> {
    return this.apiService.get("/dashboard/config").map(data => {
      return data;
    });
  }

  setDashboardConfig(newConfig) {
    return this.apiService.post("/dashboard/config", newConfig);
  }

  getTradingHistory(
    page: number,
    limit: number = 10,
    dateFrom: Date,
    dateTo: Date
  ) {
    const pars = new URLSearchParams();
    pars.set("page", page.toString());
    pars.set("limit", limit.toString());
    pars.set(
      "search",
      "transactionTime>" +
        dateFrom.toISOString() +
        ",transactionTime<" +
        dateTo.toISOString()
    );

    return this.apiService.getRaw("/trading/history", pars).map(res => {
      return {
        data: res.json(),
        totalCount: res.headers.get("content-range")
      };
    });
  }

  getTradingCommitments(dateFrom: Date, dateTo: Date) {
    const pars = new URLSearchParams();
    pars.set("dateFrom", dateFrom.toISOString());
    pars.set("dateTo", dateTo.toISOString());

    return this.apiService.get("/trading/commitments", pars);
  }

  getActivePortfolio(): Observable<any> {
    return this.apiService.get("/portfolio/");
  }

  getTradingState(): Observable<number> {
    return this.apiService.get("/trading/operationState");
  }

  getActiveTransaction(): Observable<any> {
    return this.apiService
      .getRaw("/trading/activeTransaction")
      .map((r: Response) => (r.body ? r.json() : null));
  }

  sendUpdateFMARbid(): any {
    return this.apiService.postRaw("/trading/sendUpdateBid", null);
  }

  getFlexOfferHistory(
    page: number,
    limit: number = 10,
    user: string,
    states: any,
    dateFrom: Date,
    dateTo: Date
  ) {
    const pars = new URLSearchParams();
    pars.set("page", page.toString());
    pars.set("limit", limit.toString());
    let statesQryString = "";
    for (let state of states) {
      statesQryString = statesQryString + ",status%" + state.itemName;
    }
    pars.set(
      "search",
      "creationTime>" +
        dateFrom.toISOString() +
        ",creationTime<" +
        dateTo.toISOString() +
        statesQryString
    );
    pars.set("user", user);
    return this.apiService.getRaw("/flexoffer/history", pars).map(res => {
      return {
        data: res.json(),
        totalCount: res.headers.get("content-range")
      };
    });
  }

  getTradingConfig(): Observable<any> {
    return this.apiService.get("/trading/config");
  }

  saveTradingConfig(conf): Observable<any> {
    return this.apiService.postRaw("/trading/config", conf);
  }

  getAggregatedContent(): Observable<any> {
    return this.apiService.get("/flexoffer/").map(data => {
      return data;
    });
  }

  getUserFlexOffers(): Observable<any> {
    return this.apiService.get("/flexoffer/active").map(data => {
      return data;
    });
  }

  getPortfolioFlexOffers(): Observable<any> {
    return this.apiService.get("/portfolio/flexoffers").map(data => {
      return data;
    });
  }

  getOptimizedPortfolioFlexOffers(): Observable<any> {
    return this.apiService.get("/portfolio/opt_flexoffers").map(data => {
      return data;
    });
  }

  getKPISummary(dateFrom: Date, dateTo: Date, userName: String = null) {
    const pars = new URLSearchParams();
    pars.set("dateFrom", dateFrom.toISOString());
    pars.set("dateTo", dateTo.toISOString());
    return this.apiService.get(
      "/kpis/summary" + (userName ? "/" + userName : ""),
      pars
    );
  }
}
