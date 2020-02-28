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
  selector: "flexofferview",
  template: '<svg #svgcanvas width="100%"></svg>'
})
export class FlexofferviewComponent implements OnInit, OnChanges {
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
      right: 25,
      bottom: 20,
      left: 80
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

    if (this.flexoffer == null) {
      canvas
        .append("text")
        .attr("x", 0)
        .attr("y", 0)
        .attr("text-anchor", "left")
        .style("font-size", "18px")
        .style("text-decoration", "underline")
        .text("Flex-offer is undefined!");
      return;
    }

    const timeFrom = new Date(
      this.timerange ? this.timerange[0] : this.flexoffer.startAfterTime
    );
    const timeTo = new Date(
      this.timerange ? this.timerange[1] : this.flexoffer.endBeforeTime
    );
    let energyFrom: number = this.energyrange
      ? this.energyrange[0]
      : Math.min.apply(
          0,
          this.flexoffer.flexOfferProfileConstraints.map(
            s => s.energyConstraintList[0].lower
          )
        );
    let energyTo: number = this.energyrange
      ? this.energyrange[1]
      : Math.max.apply(
          0,
          this.flexoffer.flexOfferProfileConstraints.map(
            s => s.energyConstraintList[0].upper
          )
        );

    if (Math.abs(energyFrom - energyTo) <= 1e-4) {
      energyFrom -= 1;
      energyTo += 1;
    }

    // Scales
    const xScale = d3
      .scaleTime()
      .domain([timeFrom, timeTo])
      .range([20, width]);
    const yScale = d3
      .scaleLinear()
      .domain([energyFrom, energyTo])
      .rangeRound([height, 0]);

    // Make svg Axis
    const xAxis = d3
      .axisBottom(xScale)
      .ticks(
        Math.min(
          width / this.xTickDensity,
          (this.flexoffer.flexOfferProfileConstraints || []).length
        )
      )
      .tickSizeInner(-height)
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
      .text("Consumption Energy, kWh");

    canvas
      .append("g")
      .attr("class", "foXaxis")
      .attr("transform", "translate(0," + height + ")")
      .call(xAxis);

    canvas
      .append("g")
      .attr("class", "foYaxis")
      .attr("transform", "translate(" + yAxisPadding + ",0)")
      .call(yAxis)
      .append("text")
      .attr("y", -2)
      .attr("x", -10)
      .style("text-anchor", "end")
      .text("kWh");

    // Add horizontal line, for X axis
    canvas
      .append("line")
      .attr("class", "foY0line")
      .attr("x1", xScale(timeFrom))
      .attr("x2", xScale(timeTo))
      .attr("y1", yScale(0))
      .attr("y2", yScale(0));

    /* Add time flexibility region */
    const tStart = new Date(this.flexoffer.startAfterTime),
      tEnd = new Date(this.flexoffer.startBeforeTime),
      tLEnd = new Date(this.flexoffer.endBeforeTime),
      tH = 15;

    /* Add time flexibility area and lines/text */
    canvas
      .append("rect")
      .attr("class", "foTimeFlexibility")
      .attr("x", xScale(tStart))
      .attr("y", yScale(0) - tH)
      .attr("height", tH)
      .attr("width", xScale(tEnd) - xScale(tStart));

    canvas
      .append("line")
      .attr("class", "foTimeFlexibility")
      .attr("x1", xScale(tStart))
      .attr("x2", xScale(tStart))
      .attr("y1", yScale(energyTo))
      .attr("y2", yScale(energyFrom));

    canvas
      .append("text")
      .attr("class", "foTimeFlexibility")
      .attr("x", xScale(tStart))
      .attr("y", yScale(energyTo))
      .style("text-anchor", "end")
      .text("EST:" + this.hhmm(tStart));

    canvas
      .append("line")
      .attr("class", "foTimeFlexibility")
      .attr("x1", xScale(tEnd))
      .attr("x2", xScale(tEnd))
      .attr("y1", yScale(energyTo))
      .attr("y2", yScale(energyFrom));

    canvas
      .append("text")
      .attr("class", "foTimeFlexibility")
      .attr("x", xScale(tEnd))
      .attr("y", yScale(energyTo))
      .style("text-anchor", "start")
      .text("LST:" + this.hhmm(tEnd));

    canvas
      .append("line")
      .attr("class", "foTimeFlexibility")
      .attr("x1", xScale(tLEnd))
      .attr("x2", xScale(tLEnd))
      .attr("y1", yScale(energyTo))
      .attr("y2", yScale(energyFrom));

    canvas
      .append("text")
      .attr("class", "foTimeFlexibility")
      .attr("x", xScale(tLEnd))
      .attr("y", yScale(energyTo))
      .style("text-anchor", "start")
      .text("LET:" + this.hhmm(tLEnd));

    // Add date label
    canvas
      .append("text")
      .attr("x", 0)
      .attr("y", height + margin.bottom)
      .style("text-anchor", "end")
      .text(timeFrom.toLocaleDateString("en-US"));

    // Make bars
    const barSlices = [];
    let tl =
      this.flexoffer.flexOfferSchedule !== null
        ? new Date(this.flexoffer.flexOfferSchedule.startTime)
        : this.flexoffer.defaultSchedule !== null
        ? new Date(this.flexoffer.defaultSchedule.startTime)
        : new Date(this.flexoffer.startAfterTime);

    for (
      let i = 0;
      i < this.flexoffer.flexOfferProfileConstraints.length;
      i++
    ) {
      const s = this.flexoffer.flexOfferProfileConstraints[i];
      const th = d3.timeSecond.offset(
        tl,
        s.maxDuration * this.flexoffer.numSecondsPerInterval
      );
      const sch =
        this.flexoffer.flexOfferSchedule !== null
          ? this.flexoffer.flexOfferSchedule.scheduleSlices[i].energyAmount
          : null;
      const def =
        this.flexoffer.defaultSchedule !== null
          ? this.flexoffer.defaultSchedule.scheduleSlices[i].energyAmount
          : null;

      barSlices.push({
        tl: tl,
        th: th,
        td: xScale(th) - xScale(tl),
        el: s.energyConstraintList[0].lower,
        eh: s.energyConstraintList[0].upper,
        sch: sch,
        def: def
      });
      tl = th;
    }

    const sliceGroup = canvas
      .selectAll(".slice")
      .data(barSlices)
      .enter()
      .append("g")
      .attr("class", "slice")
      .attr("transform", function(s) {
        return "translate(" + xScale(s.tl) + ",0)";
      });

    sliceGroup
      .append("rect")
      .attr("class", "foAreaBase")
      .attr("width", function(s) {
        return s.td;
      })
      .attr("y", function(s) {
        return s.eh >= 0 ? yScale(s.el) : yScale(0);
      })
      .attr("height", function(s) {
        return Math.max(
          0,
          s.eh >= 0
            ? yScale(Math.max(0, energyFrom)) - yScale(s.el)
            : yScale(s.eh) - yScale(0)
        );
      });

    sliceGroup
      .append("rect")
      .attr("class", "foAreaFlex")
      .attr("width", function(s) {
        return s.td;
      })
      .attr("y", function(s) {
        return yScale(s.eh);
      })
      .attr("height", function(s) {
        return yScale(s.el) - yScale(s.eh);
      });

    // Make default schedule line
    if (this.flexoffer.defaultSchedule !== null) {
      const _st = new Date(this.flexoffer.defaultSchedule.startTime);
      sliceGroup
        .append("line")
        .attr("class", "foDefSchedule")
        .attr("x1", 0)
        .attr("x2", function(s) {
          return s.td;
        })
        .attr("y1", function(s) {
          return yScale(s.def);
        })
        .attr("y2", function(s) {
          return yScale(s.def);
        });

      canvas
        .append("line")
        .attr("class", "foDefSchedule")
        .attr("x1", xScale(_st))
        .attr("x2", xScale(_st))
        .attr("y1", yScale(energyTo) + 15)
        .attr("y2", yScale(energyFrom));
      canvas
        .append("text")
        .attr("class", "foDefSchedule")
        .attr("x", xScale(_st))
        .attr("y", yScale(energyTo) + 30)
        .style("text-anchor", "end")
        .text("Def ST:" + this.hhmm(_st));
    }

    // Make schedule line
    if (this.flexoffer.flexOfferSchedule !== null) {
      const _st = new Date(this.flexoffer.flexOfferSchedule.startTime);
      sliceGroup
        .append("line")
        .attr("class", "foSchedule")
        .attr("x1", 0)
        .attr("x2", function(s) {
          return s.td;
        })
        .attr("y1", function(s) {
          return yScale(s.sch);
        })
        .attr("y2", function(s) {
          return yScale(s.sch);
        });

      canvas
        .append("line")
        .attr("class", "foSchedule")
        .attr("x1", xScale(_st))
        .attr("x2", xScale(_st))
        .attr("y1", yScale(energyTo))
        .attr("y2", yScale(energyFrom));
      canvas
        .append("text")
        .attr("class", "foSchedule")
        .attr("x", xScale(_st))
        .attr("y", yScale(energyTo) + 15)
        .style("text-anchor", "end")
        .text("ST:" + this.hhmm(_st));
    }
  }
}
