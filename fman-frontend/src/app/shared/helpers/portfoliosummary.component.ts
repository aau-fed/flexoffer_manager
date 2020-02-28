import { Component, Input, OnInit } from "@angular/core";

@Component({
  selector: "portfoliosummary",
  templateUrl: "portfoliosummary.component.html"
})
export class PortfolioSummaryComponent implements OnInit {
  @Input() public activePortfolio: any = {};

  ngOnInit() {}
}
