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
  
  constructor(private reportService:ReportService) { }

  // set raveFormOid for selection //
  // setRaveFormOid = (e):void => {
  //   console.log(e)
  //       this.raveFormOid = e.target.value;
  // }

  getCongruentFormTotal = forms => {
    // const f = forms.filter(v => v.congruencyStatus=='WARNINGS')
    console.log('a')
    return 'asdsa';
  }

  test(a) {
    console.log("A")
    return 'a'
  }
  ngOnInit() {
    // this.raveFormOid = 'PATIENT_ELIGIBILITY'
    this.reportService.getReportData().subscribe(data=> {console.log(data);return this.reportData = data}).unsubscribe();
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


