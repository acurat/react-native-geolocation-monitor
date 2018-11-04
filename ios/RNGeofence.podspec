
Pod::Spec.new do |s|
  s.name         = "RNGeofence"
  s.version      = "1.0.0"
  s.summary      = "RNGeofence"
  s.description  = <<-DESC
                  RNGeofence
                   DESC
  s.homepage     = ""
  s.license      = "MIT"
  # s.license      = { :type => "MIT", :file => "FILE_LICENSE" }
  s.author             = { "author" => "author@domain.cn" }
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/author/RNGeofence.git", :tag => "master" }
  s.source_files  = "RNGeofence/**/*.{h,m}"
  s.requires_arc = true


end

  