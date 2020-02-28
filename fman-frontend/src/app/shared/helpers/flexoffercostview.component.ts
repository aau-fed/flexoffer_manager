import {
  Component,
  OnInit,
  Input,
  SimpleChanges,
  OnChanges,
  ElementRef,
  ViewChild,
  HostListener
} from "@angular/core";
import * as d3 from "d3";

@Component({
  selector: "flexoffercostview",
  template: '<svg #svgcanvas width="100%"></svg>'
})
export class FlexofferCostviewComponent implements OnInit, OnChanges {
  @Input() public xTickDensity = 75;
  @Input() public flexoffer: any = null;
  @Input() public timerange: number[] = null;
  @Input() public maxWidth: number = null;
  @Input() public energyrange: number[] = null;

  @ViewChild("svgcanvas") svgcanvas: ElementRef;

  constructor() {}

  ngOnInit() {
    this.render();
  }

  ngOnChanges(changes: SimpleChanges) {
    this.render();
  }

  @HostListener("window:resize") onResize() {
    if (this.svgcanvas != null) {
      this.render();
    }
  }

  private hhmm(date: Date) {
    const hh = date.getHours().toString();
    const mm = date.getMinutes().toString();
    return (hh[1] ? hh : "0" + hh[0]) + ":" + (mm[1] ? mm : "0" + mm[0]);
  }

  public render() {
    if (this.svgcanvas == null) {
      return;
    }

    const dirWidth = Math.min(
      this.maxWidth || Number.MAX_VALUE,
      this.svgcanvas.nativeElement.clientWidth !== 0
        ? this.svgcanvas.nativeElement.clientWidth
        : window.innerWidth
    );

    // Margin and paddings
    const margin = {
      top: 40,
      right: 5,
      bottom: 20,
      left: 60
    };
    const yAxisPadding = -5;

    const height = 400 - margin.top - margin.bottom;
    const width = dirWidth - margin.right - margin.left;

    // Add the svg element to the dom
    const svg = d3.select(this.svgcanvas.nativeElement);

    // Delete old data
    svg.selectAll("*").remove();

    const canvas = svg
      .attr("width", width + margin.left + margin.right)
      .attr("height", height + margin.top + margin.bottom)
      .append("g")
      .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    if (this.flexoffer == null || this.flexoffer.defaultSchedule == null) {
      canvas
        .append("text")
        .attr("x", 0)
        .attr("y", 0)
        .attr("text-anchor", "left")
        .style("font-size", "18px")
        .style("text-decoration", "underline")
        .text("Flex-offer or its default schedule is undefined!");
      return;
    }
    const sliceCount = this.flexoffer.flexOfferProfileConstraints.length;

    const timeFrom = new Date(
      this.timerange ? this.timerange[0] : this.flexoffer.startAfterTime
    );
    const timeTo = new Date(
      this.timerange ? this.timerange[1] : this.flexoffer.endBeforeTime
    );

    /* Compute min/max delta energy and price */
    let minDEnergy = Number.MAX_VALUE,
      maxDEnergy = -Number.MAX_VALUE,
      minCost = 0,
      maxCost = 0;

    for (
      let i = 0;
      i < this.flexoffer.flexOfferProfileConstraints.length;
      i++
    ) {
      const minDE =
        this.flexoffer.flexOfferProfileConstraints[i].energyConstraintList[0]
          .lower -
        this.flexoffer.defaultSchedule.scheduleSlices[i].energyAmount;
      const maxDE =
        this.flexoffer.flexOfferProfileConstraints[i].energyConstraintList[0]
          .upper -
        this.flexoffer.defaultSchedule.scheduleSlices[i].energyAmount;
      minDEnergy = Math.min(minDEnergy, minDE);
      maxDEnergy = Math.max(maxDEnergy, maxDE);
      minCost = Math.min(
        minCost,
        maxDE *
          this.flexoffer.flexOfferProfileConstraints[i].tariffConstraint
            .maxTariff || 0
      );
      minCost = Math.min(
        minCost,
        minDE *
          this.flexoffer.flexOfferProfileConstraints[i].tariffConstraint
            .minTariff || 0
      );
      maxCost = Math.max(
        maxCost,
        Math.abs(
          maxDE *
            this.flexoffer.flexOfferProfileConstraints[i].tariffConstraint
              .maxTariff || 0
        )
      );
      maxCost = Math.max(
        maxCost,
        Math.abs(
          minDE *
            this.flexoffer.flexOfferProfileConstraints[i].tariffConstraint
              .minTariff || 0
        )
      );
    }

    // Scales
    const tScale = d3
      .scaleTime()
      .domain([timeFrom, timeTo])
      .range([0, width]);
    const yScale = d3
      .scaleLinear()
      .domain([minCost, maxCost])
      .rangeRound([height - 20, 0]);

    const xLeft = tScale(new Date(this.flexoffer.startAfterTime));
    const xRight = tScale(new Date(this.flexoffer.endAfterTime));
    const sWidth = (xRight - xLeft) / sliceCount;

    const xScale = d3
      .scaleLinear()
      .domain([minDEnergy, maxDEnergy])
      .rangeRound([0, sWidth]);

    // Make svg Axis
    const tAxis = d3
      .axisBottom(tScale)
      .ticks(sliceCount)
      .tickSizeInner(-height)
      .tickPadding(10);
    const xAxis = d3
      .axisBottom(xScale)
      .ticks(3)
      .tickSizeInner(-height - 15)
      .tickPadding(10);
    const yAxis = d3
      .axisLeft(yScale)
      .tickSizeInner(-width)
      .tickPadding(10);

    // Add FO label, if any
    canvas
      .append("text")
      .attr("x", -margin.left)
      .attr("y", -margin.top / 2)
      .style("text-anchor", "start")
      .style("font-weight", "bold")
      .text(this.flexoffer.label || this.flexoffer.id);

    // now add titles to the axes
    canvas
      .append("text")
      .attr("text-anchor", "middle")
      .attr(
        "transform",
        "translate(" + (-margin.left + 10) + "," + height / 2 + ")rotate(-90)"
      )
      .text("Deviation (schedule realization) Cost, EUR");

    canvas
      .append("g")
      .attr("class", "foTaxis")
      .attr("transform", "translate(0, " + height + ")")
      .call(tAxis);

    canvas
      .append("g")
      .attr("class", "foYaxis")
      .attr("transform", "translate(" + yAxisPadding + ",0)")
      .call(yAxis)
      .append("text")
      .attr("y", -2)
      .attr("x", -10)
      .style("text-anchor", "end")
      .text("EUR");
    //  &Delta;

    /* Add X axis*/
    for (
      let i = 0;
      i < this.flexoffer.flexOfferProfileConstraints.length;
      i++
    ) {
      const sLeft = xLeft + sWidth * i;

      canvas
        .append("g")
        .attr("class", "foXaxis")
        .attr(
          "transform",
          "translate("
            .concat(sLeft.toString())
            .concat(",")
            .concat((height - 25).toString())
            .concat(")")
        )
        .call(xAxis);

      const minDE =
        this.flexoffer.flexOfferProfileConstraints[i].energyConstraintList[0]
          .lower -
        this.flexoffer.defaultSchedule.scheduleSlices[i].energyAmount;
      const maxDE =
        this.flexoffer.flexOfferProfileConstraints[i].energyConstraintList[0]
          .upper -
        this.flexoffer.defaultSchedule.scheduleSlices[i].energyAmount;

      const minDECost =
        minDE *
          this.flexoffer.flexOfferProfileConstraints[i].tariffConstraint
            .minTariff || 0;
      const maxDECost =
        maxDE *
          this.flexoffer.flexOfferProfileConstraints[i].tariffConstraint
            .maxTariff || 0;

      canvas
        .append("line")
        .attr("class", "foCostFunction")
        .attr("x1", sLeft + xScale(minDE))
        .attr("y1", yScale(minDECost))
        .attr("x2", sLeft + xScale(0))
        .attr("y2", yScale(0));

      canvas
        .append("line")
        .attr("class", "foCostFunction")
        .attr("x1", sLeft + xScale(0))
        .attr("y1", yScale(0))
        .attr("x2", sLeft + xScale(maxDE))
        .attr("y2", yScale(maxDECost));
    }
  }
}
