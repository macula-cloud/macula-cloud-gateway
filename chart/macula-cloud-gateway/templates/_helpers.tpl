{{- define "service.image" -}}
{{- printf "%s:%s" .Values.image.repository (default (.Chart.Version) .Values.image.tag) -}}
{{- end -}}

{{/*
Return the appropriate apiVersion for deployment.
*/}}
{{- define "app.deployment.apiVersion" -}}
{{- if semverCompare "<1.9-0" .Capabilities.KubeVersion.GitVersion -}}
{{- print "apps/v1beta2" -}}
{{- else -}}
{{- print "apps/v1" -}}
{{- end -}}
{{- end -}}

{{/*
Return the appropriate apiVersion for statefulset.
*/}}
{{- define "app.statefulset.apiVersion" -}}
{{- if semverCompare "<1.9-0" .Capabilities.KubeVersion.GitVersion -}}
{{- print "apps/v1beta2" -}}
{{- else -}}
{{- print "apps/v1" -}}
{{- end -}}
{{- end -}}

{{/*
Return the appropriate apiVersion for ingress.
*/}}
{{- define "app.ingress.apiVersion" -}}
{{- if semverCompare "<1.14-0" .Capabilities.KubeVersion.GitVersion -}}
{{- print "extensions/v1beta1" -}}
{{- else -}}
{{- print "networking.k8s.io/v1beta1" -}}
{{- end -}}
{{- end -}}

{{- define "standard.label" -}}
choerodon.io/release: {{ .Release.Name | quote }}
component: scmp-cloud-po-schedule
{{- end -}}

{{- define "server.port" -}}
{{- if .Values.application.server -}}
{{ default (8380) .Values.application.server.port}}
{{- else -}}
8380
{{- end -}}
{{- end -}}

{{- define "management.server.port" -}}
{{- if .Values.application.management -}}
  {{- if .Values.application.management.server -}}
{{ default (8381) .Values.application.management.server.port }}
  {{- else -}}
8381
  {{- end -}}
{{- else -}}
8381
{{- end -}}
{{- end -}}


{{- define "match.label" -}}
choerodon.io/release: {{ .Release.Name | quote }}
{{- end -}}

{{- define "pod.labels" -}}
choerodon.io/version: {{ .Chart.Version | quote }}
choerodon.io/service: scmp-cloud-po-schedule
choerodon.io/metrics-port: {{ include "management.server.port" . | quote }}
{{ include "standard.label" . }}
{{- end -}}

{{- define "logging.label" -}}
{{- if .Values.logs -}}
choerodon.io/logs-parser: {{ .Values.logs.parser }}
{{- end -}}
{{- end -}}

{{- define "monitoring.annotations" -}}
{{- if .Values.metrics -}}
choerodon.io/metrics-group: {{ .Values.metrics.group | quote }}
choerodon.io/metrics-path: {{ .Values.metrics.path | quote }}
{{- end -}}
{{- end -}}