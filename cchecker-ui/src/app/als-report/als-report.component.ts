import { Component, OnInit } from '@angular/core';
import { Observable } from '../../../node_modules/rxjs';
import { ReportService } from '../services/report.service';
import { RouterLinkWithHref } from '../../../node_modules/@angular/router';

@Component({
  selector: 'app-als-report',
  templateUrl: './als-report.component.html',
  styleUrls: ['./als-report.component.css']
})
export class AlsReportComponent implements OnInit {
  reportData:Object;
  dtOptions:Object;
  congruentFormTotal:String;

  constructor(private reportService:ReportService) { }

  changeForm = form => console.log("A");

  ngOnInit() {
    this.reportService.getReportData().subscribe(data=> this.reportData = data).unsubscribe();
    this.congruentFormTotal = this.reportData['cccForms'].filter(v => v.congruencyStatus=='CONGRUENT').length;
    this.dtOptions={
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
      rowCallback: (r,d,e) => {
        $('td', r).unbind('click');
        $('td', r).bind('click', () => {
          console.log("A")
        });
        return r;
      },

      ordering:false,
      paging:false,
      searching:false,
      info:false
    };

  };

}


