import { Component, OnInit, Input } from '@angular/core';
@Component({
  selector: 'app-progress-bar',
  templateUrl: './progress-bar.component.html',
  styleUrls: ['./progress-bar.component.css']
})
export class ProgressBarComponent implements OnInit {
  @Input() percentage:Number;
  @Input() statusMessage:String;
  @Input() type:String;

  constructor()   {
  }

  // get progress bar text //
  getProgress = () => {
    if (this.statusMessage) {
      return this.statusMessage;
    }
    else {
      this.percentage<100 ? `Uploading`:'Complete'
    }
  }

  ngOnInit() {
  }
}
