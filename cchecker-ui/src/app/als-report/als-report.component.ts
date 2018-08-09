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
  private reportData:Observable<Object>;
  private dtOptions:Object;
  constructor(private reportService:ReportService) { }

  ngOnInit() {
    this.reportData = this.reportService.getReportData();
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
  }

}
