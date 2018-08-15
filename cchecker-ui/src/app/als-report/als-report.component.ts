import { Component, OnInit, ViewChild } from '@angular/core';
import { ReportService } from '../services/report.service';
import { NgbTabset } from '../../../node_modules/@ng-bootstrap/ng-bootstrap';
import { RestService } from '../services/rest.service';
import { HttpClient } from '../../../node_modules/@angular/common/http';
import { saveAs }  from 'file-saver'

@Component({
  selector: 'app-als-report',
  templateUrl: './als-report.component.html',
  styleUrls: ['./als-report.component.css']
})
export class AlsReportComponent implements OnInit {
  congruentFormTotal:String;
  dtFormOptions:Object;
  dtFormSummaryOptions:Object;
  dtSummaryOptions:Object;
  error:boolean;
  errorMessage:String;  
  raveForm:Object;
  reportData:Object;
  showFormTab:Boolean=false;
  statusMessage:String;
  tabName:String;
  @ViewChild(NgbTabset)
    tabs: NgbTabset;

  constructor(private reportService:ReportService, private restService:RestService,private http:HttpClient) { }

  // switch tab when selecting form from dropdown //
  changeForm = (form) => {
    this.tabName = form.raveFormOid;
    this.showFormTab = true;
    this.raveForm = form;
    this.tabs.select('raveForm');
  }

  generateExcel = () => {
    this.statusMessage = null;
    this.error = false;
    this.restService.generateExcel().subscribe(
      data => {
        const filename = data.headers.get('Content-Disposition').replace('attachment; filename=','')
        var blob = new Blob([data.body], {type: "application/vnd.ms-excel"});
        saveAs(blob, filename);
        this.statusMessage = `${filename} downloaded succesfully.`
      }, 
      error => {
        this.error = true;
        const reader: FileReader = new FileReader();  
        reader.readAsText(error.error)
        reader.onloadend = (error):void => this.errorMessage = reader.result;
      },
      () => console.log("FINISHED"))
  };
  
  ngOnInit() {
    // this.restService.getUrl().subscribe(data=>console.log(data))
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
      columns: [{width:"70px"},{width:"80px"},{width:"80px"},{width:"80px"},{width:"150px"},{width:"300px"},{width:"300px"},{width:"200px"},{width:"700px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"400px"},{width:"700px"},{width:"400px"},{width:"700px"},{width:"120px"},{width:"500px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"120px"}],
      ordering:false,
      paging:false,
      searching:false,
      info:false,
      scrollX:true,
      scrollY:300,
      scroller:true
    };
  };

}


