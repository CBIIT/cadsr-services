import { Component, OnInit, Input } from '@angular/core';
@Component({
  selector: 'app-progress-bar',
  templateUrl: './progress-bar.component.html',
  styleUrls: ['./progress-bar.component.css']
})
export class ProgressBarComponent implements OnInit {
  @Input() config:Object;
  @Input() percentage:Number;

  constructor()   {
  }

  getProgress() {
    return this.percentage<100 ? `Progress: ${this.percentage}%`:'Complete'
  }

  ngOnInit() {
    console.log(this.percentage);
  }
}
