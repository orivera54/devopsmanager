import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Pipe({
  name: 'safeHtml',
  standalone: true // Marking pipe as standalone
})
export class SafeHtmlPipe implements PipeTransform {
  constructor(private sanitizer: DomSanitizer) {}

  transform(value: string | undefined | null): SafeHtml {
    if (value === null || value === undefined) {
      return ''; // Or this.sanitizer.bypassSecurityTrustHtml('');
    }
    // Basic check if it looks like HTML, otherwise, might want to escape it or handle differently.
    // For now, trusting the value if it's provided.
    return this.sanitizer.bypassSecurityTrustHtml(value);
  }
}
