import { Component, OnInit, ViewChild } from '@angular/core';
import { Observable } from '../../../node_modules/rxjs';
import { ReportService } from '../services/report.service';
import { RouterLinkWithHref } from '../../../node_modules/@angular/router';
import { NgbTabset } from '../../../node_modules/@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-als-report',
  templateUrl: './als-report.component.html',
  styleUrls: ['./als-report.component.css']
})
export class AlsReportComponent implements OnInit {
  reportData:Object;
  dtSummaryOptions:Object;
  dtFormOptions:Object;
  dtFormSummaryOptions:Object;
  congruentFormTotal:String;
  showFormTab:Boolean=false;
  tabName:String;
  raveForm:Object;
  @ViewChild(NgbTabset)
    tabs: NgbTabset;
  constructor(private reportService:ReportService) { }


  changeForm = (form) => {
    this.tabName = form.raveFormOid;
    this.showFormTab = true;
    this.raveForm = form;
    this.tabs.select('raveForm');

  }

  ngOnInit() {
    this.reportService.getReportData().subscribe(data=> this.reportData = data).unsubscribe();
    this.congruentFormTotal = this.reportData['cccForms'].filter(v => v.congruencyStatus=='CONGRUENT').length;
    this.dtSummaryOptions={
      columns: [
        {
          width:"50%",
          cellType:"th"
        },
        {
          width:"50%",
          cellType:"th"
        },
      ],
      ordering:false,
      paging:false,
      searching:false,
      info:false
    };
    this.dtFormSummaryOptions={
      columns: [
        {
          width:"25%",
          cellType:"th"
        },
        {
          width:"75%",
          cellType:"th"
        },
      ],
      ordering:false,
      paging:false,
      searching:false,
      info:false
    };
    this.dtFormOptions={
      columns: [
        {width:"70px"},
        {width:"80px"},
        {width:"80px"},
        {width:"80px"},
        {width:"150px"},
        {width:"300px"},
        {width:"300px"},
        {width:"200px"},
        {width:"700px"},


        {width:"120px"},
        {width:"120px"},
        {width:"120px"},
        {width:"400px"},
        {width:"700px"},
        {width:"400px"},
        {width:"700px"},
        {width:"120px"},
        {width:"500px"},
        {width:"120px"},
        {width:"120px"},
        {width:"120px"},
        {width:"120px"},
        {width:"120px"},
        {width:"120px"},
        {width:"120px"},
        {width:"120px"},
        {width:"120px"},
        {width:"120px"},
        {width:"120px"},
        {width:"120px"},
                  
     
      ],
      ordering:false,
      paging:false,
      searching:false,
      info:false,
      scrollX:true,
      scroller:true
    };
  };

}


