import { Component, OnInit } from '@angular/core';
import { Observable } from '../../../node_modules/rxjs';
import { ReportService } from '../services/report.service';

@Component({
  selector: 'app-als-report',
  templateUrl: './als-report.component.html',
  styleUrls: ['./als-report.component.css']
})
export class AlsReportComponent implements OnInit {
  private reportData:Observable<Object>;

  constructor(private reportService:ReportService) { }

  ngOnInit() {
    this.reportData = this.reportService.getReportData();
  }

}
